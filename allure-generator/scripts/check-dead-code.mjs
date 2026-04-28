import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import ts from "typescript";

const scriptFile = fileURLToPath(import.meta.url);
const scriptDir = path.dirname(scriptFile);
const projectRoot = path.resolve(scriptDir, "..");
const sourceRoot = path.join(projectRoot, "src/main/javascript");
const sourceEntry = path.join(sourceRoot, "index.mts");
const consumerRoots = [
  sourceRoot,
  path.join(projectRoot, "tests"),
  path.join(projectRoot, "scripts"),
];
const consumerFiles = new Set([path.join(projectRoot, "playwright.config.mts")]);
const runtimeSourceFiles = new Set();
const sourceRecords = new Map();
const importRecords = new Map();
const excludedDeadModulePatterns = [/\/types\//];
const excludedUnusedExportPatterns = [/\/types\//, /\/index\.mts$/];

const runtimeExportKinds = new Set([
  ts.SyntaxKind.ClassDeclaration,
  ts.SyntaxKind.EnumDeclaration,
  ts.SyntaxKind.FunctionDeclaration,
  ts.SyntaxKind.VariableStatement,
]);

const collectFiles = (root, predicate) => {
  if (!fs.existsSync(root)) {
    return;
  }

  const queue = [root];
  while (queue.length > 0) {
    const current = queue.pop();
    const stat = fs.statSync(current);

    if (stat.isDirectory()) {
      fs.readdirSync(current).forEach((entry) => queue.push(path.join(current, entry)));
      continue;
    }

    if (predicate(current)) {
      consumerFiles.add(path.normalize(current));
    }
  }
};

collectFiles(sourceRoot, (file) => file.endsWith(".mts") || file.endsWith(".d.ts"));
collectFiles(path.join(projectRoot, "tests"), (file) => file.endsWith(".mts"));
collectFiles(path.join(projectRoot, "scripts"), (file) => file.endsWith(".mts") || file.endsWith(".mjs"));

Array.from(consumerFiles)
  .filter((file) => file.startsWith(sourceRoot) && file.endsWith(".mts"))
  .forEach((file) => runtimeSourceFiles.add(path.normalize(file)));

const hasRuntimeExportModifier = (node) =>
  ts.canHaveModifiers(node) &&
  ts
    .getModifiers(node)
    ?.some(
      (modifier) =>
        modifier.kind === ts.SyntaxKind.ExportKeyword || modifier.kind === ts.SyntaxKind.DefaultKeyword,
    );

const hasDefaultModifier = (node) =>
  ts.canHaveModifiers(node) &&
  ts.getModifiers(node)?.some((modifier) => modifier.kind === ts.SyntaxKind.DefaultKeyword);

const addBindingNames = (name, output) => {
  if (ts.isIdentifier(name)) {
    output.push(name.text);
    return;
  }

  if (ts.isObjectBindingPattern(name) || ts.isArrayBindingPattern(name)) {
    name.elements.forEach((element) => {
      if (ts.isBindingElement(element)) {
        addBindingNames(element.name, output);
      }
    });
  }
};

const getImportTypeQualifierName = (qualifier) => {
  if (!qualifier) {
    return null;
  }

  if (ts.isIdentifier(qualifier)) {
    return qualifier.text;
  }

  if (ts.isQualifiedName(qualifier)) {
    return qualifier.right.text;
  }

  return null;
};

const readSourceFile = (filePath) =>
  ts.createSourceFile(
    filePath,
    fs.readFileSync(filePath, "utf8"),
    ts.ScriptTarget.Latest,
    true,
    filePath.endsWith(".d.ts") ? ts.ScriptKind.TS : ts.ScriptKind.TS,
  );

const resolveModule = (fromFile, specifier) => {
  if (!specifier.startsWith(".")) {
    return null;
  }

  const base = path.normalize(path.resolve(path.dirname(fromFile), specifier));
  const candidates = [
    base,
    `${base}.mts`,
    `${base}.mjs`,
    `${base}.d.ts`,
    path.join(base, "index.mts"),
    path.join(base, "index.mjs"),
    path.join(base, "index.d.ts"),
  ];

  for (const candidate of candidates) {
    if (fs.existsSync(candidate) && fs.statSync(candidate).isFile()) {
      return path.normalize(candidate);
    }
  }

  return null;
};

const getImportRecord = (file) => {
  if (!importRecords.has(file)) {
    importRecords.set(file, {
      edges: new Set(),
      namespaceUses: new Set(),
      symbolUses: new Map(),
    });
  }

  return importRecords.get(file);
};

const markSymbolUse = (fromFile, targetFile, exportName) => {
  const record = getImportRecord(fromFile);
  record.edges.add(targetFile);

  if (!record.symbolUses.has(targetFile)) {
    record.symbolUses.set(targetFile, new Set());
  }

  record.symbolUses.get(targetFile).add(exportName);
};

const markNamespaceUse = (fromFile, targetFile) => {
  const record = getImportRecord(fromFile);
  record.edges.add(targetFile);
  record.namespaceUses.add(targetFile);
};

const markEdge = (fromFile, targetFile) => {
  getImportRecord(fromFile).edges.add(targetFile);
};

const getExportRecord = (file) => {
  if (!sourceRecords.has(file)) {
    sourceRecords.set(file, {
      exports: new Set(),
    });
  }

  return sourceRecords.get(file);
};

const addLocalExportsFromNamedClause = (file, clause) => {
  clause.elements.forEach((element) => {
    if (!element.isTypeOnly) {
      getExportRecord(file).exports.add(element.name.text);
    }
  });
};

const parseFile = (file) => {
  const sourceFile = readSourceFile(file);

  const visit = (node) => {
    if (
      ts.isImportTypeNode(node) &&
      ts.isLiteralTypeNode(node.argument) &&
      ts.isStringLiteral(node.argument.literal)
    ) {
      const targetFile = resolveModule(file, node.argument.literal.text);
      const qualifierName = getImportTypeQualifierName(node.qualifier);

      if (targetFile) {
        markEdge(file, targetFile);

        if (qualifierName) {
          markSymbolUse(file, targetFile, qualifierName);
        } else {
          markNamespaceUse(file, targetFile);
        }
      }
    }

    ts.forEachChild(node, visit);
  };

  visit(sourceFile);

  sourceFile.statements.forEach((statement) => {
    if (ts.isImportDeclaration(statement) && ts.isStringLiteral(statement.moduleSpecifier)) {
      const targetFile = resolveModule(file, statement.moduleSpecifier.text);
      if (!targetFile) {
        return;
      }

      markEdge(file, targetFile);

      const { importClause } = statement;
      if (!importClause) {
        return;
      }

      if (importClause.name) {
        markSymbolUse(file, targetFile, "default");
      }

      if (!importClause.namedBindings) {
        return;
      }

      if (ts.isNamespaceImport(importClause.namedBindings)) {
        markNamespaceUse(file, targetFile);
        return;
      }

      importClause.namedBindings.elements.forEach((element) => {
        markSymbolUse(file, targetFile, element.propertyName?.text ?? element.name.text);
      });
      return;
    }

    if (ts.isExportDeclaration(statement)) {
      if (!statement.moduleSpecifier || !ts.isStringLiteral(statement.moduleSpecifier)) {
        if (statement.exportClause && ts.isNamedExports(statement.exportClause)) {
          addLocalExportsFromNamedClause(file, statement.exportClause);
        }
        return;
      }

      const targetFile = resolveModule(file, statement.moduleSpecifier.text);
      if (!targetFile) {
        return;
      }

      if (!statement.exportClause) {
        markNamespaceUse(file, targetFile);
        return;
      }

      if (ts.isNamespaceExport(statement.exportClause)) {
        markNamespaceUse(file, targetFile);
        return;
      }

      statement.exportClause.elements.forEach((element) => {
        markSymbolUse(file, targetFile, element.propertyName?.text ?? element.name.text);
      });
      return;
    }

    if (ts.isExportAssignment(statement) && !statement.isExportEquals) {
      getExportRecord(file).exports.add("default");
      return;
    }

    if (!runtimeExportKinds.has(statement.kind) || !hasRuntimeExportModifier(statement)) {
      return;
    }

    if (ts.isVariableStatement(statement)) {
      if (hasDefaultModifier(statement)) {
        getExportRecord(file).exports.add("default");
        return;
      }

      const names = [];
      statement.declarationList.declarations.forEach((declaration) => addBindingNames(declaration.name, names));
      names.forEach((name) => getExportRecord(file).exports.add(name));
      return;
    }

    if (hasDefaultModifier(statement)) {
      getExportRecord(file).exports.add("default");
      return;
    }

    if ("name" in statement && statement.name && ts.isIdentifier(statement.name)) {
      getExportRecord(file).exports.add(statement.name.text);
    }
  });
};

consumerFiles.forEach((file) => parseFile(file));

const usageBySourceFile = new Map();

runtimeSourceFiles.forEach((file) => {
  usageBySourceFile.set(file, {
    namespaceUsed: false,
    usedExports: new Set(),
  });
});

importRecords.forEach((record) => {
  record.namespaceUses.forEach((targetFile) => {
    if (usageBySourceFile.has(targetFile)) {
      usageBySourceFile.get(targetFile).namespaceUsed = true;
    }
  });

  record.symbolUses.forEach((symbols, targetFile) => {
    if (!usageBySourceFile.has(targetFile)) {
      return;
    }

    const usage = usageBySourceFile.get(targetFile);
    symbols.forEach((symbol) => usage.usedExports.add(symbol));
  });
});

const reachableSourceFiles = new Set();
const queue = [path.normalize(sourceEntry)];

while (queue.length > 0) {
  const current = queue.pop();
  if (!current || reachableSourceFiles.has(current)) {
    continue;
  }

  reachableSourceFiles.add(current);

  const record = importRecords.get(current);
  if (!record) {
    continue;
  }

  record.edges.forEach((nextFile) => {
    if (runtimeSourceFiles.has(nextFile)) {
      queue.push(nextFile);
    }
  });
}

const shouldSkipModule = (file, patterns) => patterns.some((pattern) => pattern.test(file));
const errors = [];

Array.from(runtimeSourceFiles)
  .filter((file) => !reachableSourceFiles.has(file))
  .filter((file) => !shouldSkipModule(file, excludedDeadModulePatterns))
  .sort()
  .forEach((file) => {
    errors.push(
      `dead source module is not reachable from src/main/javascript/index.mts: ${path.relative(projectRoot, file)}`,
    );
  });

Array.from(runtimeSourceFiles)
  .filter((file) => reachableSourceFiles.has(file))
  .filter((file) => !shouldSkipModule(file, excludedUnusedExportPatterns))
  .sort()
  .forEach((file) => {
    const exportRecord = sourceRecords.get(file);
    if (!exportRecord || exportRecord.exports.size === 0) {
      return;
    }

    const usage = usageBySourceFile.get(file);
    if (!usage || usage.namespaceUsed) {
      return;
    }

    Array.from(exportRecord.exports)
      .filter((exportName) => !usage.usedExports.has(exportName))
      .sort()
      .forEach((exportName) => {
        errors.push(
          `unused exported runtime symbol "${exportName}" in ${path.relative(projectRoot, file)}`,
        );
      });
  });

if (errors.length > 0) {
  console.error("Dead-code guard failed:");
  errors.forEach((error) => console.error(`  - ${error}`));
  process.exit(1);
}
