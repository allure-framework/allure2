import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const scriptFile = fileURLToPath(import.meta.url);
const scriptDir = path.dirname(scriptFile);
const defaultStylesRoot = path.resolve(scriptDir, "../src/main/javascript");

const spacingProperties = new Set([
  "margin",
  "margin-top",
  "margin-right",
  "margin-bottom",
  "margin-left",
  "margin-inline",
  "margin-inline-start",
  "margin-inline-end",
  "margin-block",
  "margin-block-start",
  "margin-block-end",
  "padding",
  "padding-top",
  "padding-right",
  "padding-bottom",
  "padding-left",
  "padding-inline",
  "padding-inline-start",
  "padding-inline-end",
  "padding-block",
  "padding-block-start",
  "padding-block-end",
  "gap",
  "row-gap",
  "column-gap",
  "top",
  "right",
  "bottom",
  "left",
  "inset",
  "inset-inline",
  "inset-inline-start",
  "inset-inline-end",
  "inset-block",
  "inset-block-start",
  "inset-block-end",
]);

const sizeProperties = new Set([
  "width",
  "height",
  "min-width",
  "min-height",
  "max-width",
  "max-height",
  "inline-size",
  "block-size",
  "min-inline-size",
  "min-block-size",
  "max-inline-size",
  "max-block-size",
  "line-height",
  "border-radius",
]);

const declarationPattern = /^\s*([a-z-]+)\s*:\s*([^;]+);/;
const variablePattern = /^\s*(\$[a-z0-9-]+)\s*:\s*([^;]+);/i;
const spacingLiteralPattern = /(-?\d+)px\b/g;
const variableReferencePattern = /\$[a-z0-9-]+/gi;

const allowedDeclarationPatterns = [
  {
    filePattern: /\/shared\/ui\/styles\/PopoverView\.scss$/,
    property: "left",
    valuePattern:
      /^calc\(-#\{(?:space\(2\)|\$popover-arrow-size)\} \+ #\{(?:\$space-micro-1|\$popover-border-width)\}\)$/,
  },
  {
    filePattern: /\/shared\/ui\/styles\/PopoverView\.scss$/,
    property: "left",
    valuePattern: /^calc\(100% - (?:1px|#\{\$popover-border-width\})\)$/,
  },
  {
    filePattern: /\/shared\/ui\/styles\/TooltipView\.scss$/,
    property: "top",
    valuePattern: /^\$space-micro-4$/,
  },
  {
    filePattern: /\/shared\/ui\/styles\/LoaderView\.scss$/,
    property: "margin",
    valuePattern: /^-1px$/,
  },
  {
    filePattern: /\/shared\/ui\/styles\/BaseChartView\.scss$/,
    property: "top",
    valuePattern: /^-1px$/,
  },
  {
    filePattern: /\/shared\/ui\/styles\/BaseChartView\.scss$/,
    property: "border-radius",
    valuePattern: /^3px$/,
  },
  {
    filePattern: /\/shared\/ui\/styles\/BaseChartView\.scss$/,
    property: "width",
    valuePattern: /^20px$/,
  },
  {
    filePattern: /\/shared\/ui\/styles\/BaseChartView\.scss$/,
    property: "height",
    valuePattern: /^16px$/,
  },
  {
    filePattern: /\/shared\/ui\/styles\/BaseChartView\.scss$/,
    property: "margin-right",
    valuePattern: /^5px$/,
  },
  {
    filePattern: /\/rtl\.scss$/,
    property: "width",
    valuePattern: /^100px$/,
  },
  {
    filePattern: /\/shared\/styles\/primitives\/label\/styles\.scss$/,
    property: "padding",
    valuePattern: /^\$space-micro-2 \$space-micro-4$/,
  },
  {
    filePattern: /\/features\/tree\/controls\/StatusToggleView\.scss$/,
    property: "padding",
    valuePattern: /^\$space-micro-2 \$space-micro-4$/,
  },
  {
    filePattern: /\/features\/tree\/controls\/StatusToggleView\.scss$/,
    property: "padding",
    valuePattern: /^\$space-micro-1 \$space-micro-3 \$space-micro-1 \$space-micro-4$/,
  },
  {
    filePattern: /\/features\/tree\/controls\/MarksToggleView\.scss$/,
    property: "padding",
    valuePattern: /^\$space-micro-2 \$space-micro-4$/,
  },
  {
    filePattern: /\/features\/tree\/controls\/MarksToggleView\.scss$/,
    property: "padding",
    valuePattern: /^\$space-micro-1 \$space-micro-3 \$space-micro-1 \$space-micro-4$/,
  },
];

const findScssFiles = (root) => {
  const files = [];
  const queue = [root];

  while (queue.length > 0) {
    const current = queue.pop();

    if (!current || !fs.existsSync(current)) {
      continue;
    }

    const stat = fs.statSync(current);

    if (stat.isDirectory()) {
      fs.readdirSync(current).forEach((entry) => queue.push(path.join(current, entry)));
      continue;
    }

    if (current.endsWith(".scss")) {
      files.push(current);
    }
  }

  return files.sort();
};

const stripInlineComments = (line) => line.replace(/\/\/.*$/, "");

const normalizeExpression = (rawValue) => rawValue.replace(/#\{\s*([^}]+?)\s*\}/g, "$1").trim();

const stripOuterParentheses = (rawValue) => {
  let value = rawValue.trim();

  while (value.startsWith("(") && value.endsWith(")")) {
    let depth = 0;
    let balanced = true;

    for (let index = 0; index < value.length; index += 1) {
      const character = value[index];

      if (character === "(") {
        depth += 1;
      }

      if (character === ")") {
        depth -= 1;

        if (depth === 0 && index < value.length - 1) {
          balanced = false;
          break;
        }
      }
    }

    if (!balanced || depth !== 0) {
      break;
    }

    value = value.slice(1, -1).trim();
  }

  return value;
};

const parsePixelLiteral = (rawValue) => {
  const match = rawValue.match(/^(-?\d+)px$/);

  if (!match) {
    return null;
  }

  return Number.parseInt(match[1], 10);
};

const isSingleSpacingTokenExpression = (rawValue) => /^(?:space|grid)\([^)]*\)$/.test(rawValue);

const hasDirectSpacingToken = (rawValue) =>
  /\b(?:space|grid)\([^)]*\)/.test(normalizeExpression(rawValue));

const hasArithmeticSyntax = (rawValue) => {
  const expression = normalizeExpression(rawValue);
  return expression.includes("calc(") || /[+*/]/.test(expression) || /\s-\s/.test(expression);
};

const getVariableReferences = (rawValue) =>
  Array.from(new Set(normalizeExpression(rawValue).match(variableReferencePattern) ?? []));

const getPxValues = (rawValue) =>
  Array.from(rawValue.matchAll(spacingLiteralPattern), ([, value]) => Number.parseInt(value, 10));

const createVariableRecords = (lines) => {
  const variables = new Map();

  lines.forEach((line, index) => {
    const match = stripInlineComments(line).match(variablePattern);

    if (!match) {
      return;
    }

    const [, name, rawValue] = match;
    variables.set(name, {
      line: index + 1,
      rawValue: rawValue.trim(),
    });
  });

  return variables;
};

const isAllowedSpacingValue = (value) => Math.abs(value) % 8 === 0;

const isAllowedSizeValue = (value) => {
  const absoluteValue = Math.abs(value);
  return absoluteValue <= 4 || absoluteValue % 8 === 0;
};

const isAllowedDeclarationException = (file, property, rawValue) => {
  const normalizedFile = file.split(path.sep).join("/");
  return allowedDeclarationPatterns.some(
    ({ filePattern, property: allowedProperty, valuePattern }) =>
      filePattern.test(normalizedFile) &&
      property === allowedProperty &&
      valuePattern.test(rawValue.trim()),
  );
};

const createVariableInfoResolver = (variableRecords) => {
  const cache = new Map();
  const resolving = new Set();

  const resolve = (name) => {
    if (cache.has(name)) {
      return cache.get(name);
    }

    if (resolving.has(name)) {
      return { kind: "other" };
    }

    const record = variableRecords.get(name);

    if (!record) {
      return { kind: "other" };
    }

    resolving.add(name);

    const normalized = stripOuterParentheses(normalizeExpression(record.rawValue));
    const directPixelValue = parsePixelLiteral(normalized);

    if (directPixelValue !== null) {
      const info = { kind: "px", value: directPixelValue };
      cache.set(name, info);
      resolving.delete(name);
      return info;
    }

    if (isSingleSpacingTokenExpression(normalized)) {
      const info = { kind: "token" };
      cache.set(name, info);
      resolving.delete(name);
      return info;
    }

    if (/^\$[a-z0-9-]+$/i.test(normalized)) {
      const info = resolve(normalized);
      cache.set(name, info);
      resolving.delete(name);
      return info;
    }

    const referencedVariables = getVariableReferences(record.rawValue).map((reference) =>
      resolve(reference),
    );
    const referencesSpacingToken =
      hasDirectSpacingToken(record.rawValue) ||
      referencedVariables.some(({ kind }) => kind === "token" || kind === "spacing-arithmetic");

    const referencesPxValue =
      getPxValues(record.rawValue).length > 0 ||
      referencedVariables.some(({ kind }) => kind === "px" || kind === "compound");

    let info = { kind: "other" };

    if (referencesSpacingToken && hasArithmeticSyntax(record.rawValue)) {
      info = { kind: "spacing-arithmetic" };
    } else if (referencesSpacingToken || referencesPxValue) {
      info = { kind: "compound" };
    }

    cache.set(name, info);
    resolving.delete(name);
    return info;
  };

  return resolve;
};

const resolveSingleScalar = (rawValue, resolveVariableInfo) => {
  let expression = normalizeExpression(rawValue);
  let sign = 1;

  while (expression.startsWith("-")) {
    sign *= -1;
    expression = expression.slice(1).trim();
  }

  expression = stripOuterParentheses(expression);

  const pixelValue = parsePixelLiteral(expression);

  if (pixelValue !== null) {
    return { kind: "px", value: sign * pixelValue };
  }

  if (isSingleSpacingTokenExpression(expression)) {
    return { kind: "token" };
  }

  if (/^\$[a-z0-9-]+$/i.test(expression)) {
    const resolved = resolveVariableInfo(expression);

    if (resolved.kind === "px") {
      return {
        kind: "px",
        value: sign * resolved.value,
      };
    }

    return resolved;
  }

  return null;
};

const isAllowedViewportCompensation = (rawValue, resolveVariableInfo) => {
  const normalized = normalizeExpression(rawValue);
  const match = normalized.match(/^calc\((100vh|100dvh) - (.+)\)$/);

  if (!match) {
    return false;
  }

  const offset = resolveSingleScalar(match[2], resolveVariableInfo);
  return offset?.kind === "token";
};

const formatProblem = (file, line, property, rawValue, reason, stylesRoot) => ({
  file: path.relative(path.resolve(stylesRoot, "..", ".."), file),
  line,
  property,
  rawValue,
  reason,
});

const inspectScssFile = (file, stylesRoot, sharedVariableRecords) => {
  const lines = fs.readFileSync(file, "utf8").split("\n");
  const localVariableRecords = createVariableRecords(lines);
  const variableRecords = new Map([...sharedVariableRecords, ...localVariableRecords]);
  const resolveVariableInfo = createVariableInfoResolver(variableRecords);
  const problems = [];

  localVariableRecords.forEach(({ line, rawValue }, name) => {
    if (resolveVariableInfo(name).kind !== "spacing-arithmetic") {
      return;
    }

    problems.push(
      formatProblem(
        file,
        line,
        name,
        rawValue,
        "uses spacing arithmetic; keep spacing variables as direct grid tokens",
        stylesRoot,
      ),
    );
  });

  lines.forEach((line, index) => {
    const match = stripInlineComments(line).match(declarationPattern);

    if (!match) {
      return;
    }

    const [, property, rawValue] = match;
    const trimmedValue = rawValue.trim();
    const isSpacingProperty = spacingProperties.has(property);
    const isSizeProperty = sizeProperties.has(property);

    if (!isSpacingProperty && !isSizeProperty) {
      return;
    }

    if (isAllowedDeclarationException(file, property, trimmedValue)) {
      return;
    }

    const variableInfos = getVariableReferences(trimmedValue).map((reference) => ({
      name: reference,
      ...resolveVariableInfo(reference),
    }));
    const usesSpacingReferences =
      hasDirectSpacingToken(trimmedValue) ||
      variableInfos.some(({ kind }) => kind === "token" || kind === "spacing-arithmetic");

    if (
      !isAllowedViewportCompensation(trimmedValue, resolveVariableInfo) &&
      (variableInfos.some(({ kind }) => kind === "spacing-arithmetic") ||
        (usesSpacingReferences && hasArithmeticSyntax(trimmedValue)))
    ) {
      problems.push(
        formatProblem(
          file,
          index + 1,
          property,
          trimmedValue,
          "uses spacing arithmetic; prefer direct grid tokens or a border-compensation exception",
          stylesRoot,
        ),
      );
      return;
    }

    const singleScalar = resolveSingleScalar(trimmedValue, resolveVariableInfo);

    if (isSpacingProperty) {
      if (singleScalar?.kind === "px" && !isAllowedSpacingValue(singleScalar.value)) {
        problems.push(
          formatProblem(
            file,
            index + 1,
            property,
            trimmedValue,
            `bad px value: ${singleScalar.value}`,
            stylesRoot,
          ),
        );
        return;
      }

      const invalidLiteralValues = getPxValues(trimmedValue).filter(
        (value) => !isAllowedSpacingValue(value),
      );

      if (invalidLiteralValues.length > 0) {
        problems.push(
          formatProblem(
            file,
            index + 1,
            property,
            trimmedValue,
            `bad px values: ${invalidLiteralValues.join(", ")}`,
            stylesRoot,
          ),
        );
        return;
      }

      if (variableInfos.some(({ kind }) => kind === "px" || kind === "compound")) {
        problems.push(
          formatProblem(
            file,
            index + 1,
            property,
            trimmedValue,
            "must resolve to direct grid tokens for spacing properties",
            stylesRoot,
          ),
        );
      }

      return;
    }

    if (singleScalar?.kind === "px" && !isAllowedSizeValue(singleScalar.value)) {
      problems.push(
        formatProblem(
          file,
          index + 1,
          property,
          trimmedValue,
          `bad px value: ${singleScalar.value}`,
          stylesRoot,
        ),
      );
      return;
    }

    const invalidLiteralValues = getPxValues(trimmedValue).filter(
      (value) => !isAllowedSizeValue(value),
    );

    if (invalidLiteralValues.length > 0) {
      problems.push(
        formatProblem(
          file,
          index + 1,
          property,
          trimmedValue,
          `bad px values: ${invalidLiteralValues.join(", ")}`,
          stylesRoot,
        ),
      );
    }
  });

  return problems;
};

export const inspectSpacingGrid = (stylesRoot = defaultStylesRoot) => {
  const sharedVariablesPath = path.join(stylesRoot, "variables.scss");
  const sharedVariableRecords = fs.existsSync(sharedVariablesPath)
    ? createVariableRecords(fs.readFileSync(sharedVariablesPath, "utf8").split("\n"))
    : new Map();

  return findScssFiles(stylesRoot).flatMap((file) =>
    inspectScssFile(file, stylesRoot, sharedVariableRecords),
  );
};

export const runSpacingGridCheck = (stylesRoot = defaultStylesRoot) => {
  const problems = inspectSpacingGrid(stylesRoot);

  if (problems.length === 0) {
    return;
  }

  console.error(
    "Found non-grid spacing or hidden spacing arithmetic. Use direct grid tokens and explicit exceptions.\n",
  );
  problems.forEach(({ file, line, property, rawValue, reason }) => {
    console.error(`${file}:${line} ${property}: ${rawValue} (${reason})`);
  });
  process.exit(1);
};

if (process.argv[1] && path.resolve(process.argv[1]) === scriptFile) {
  runSpacingGridCheck();
}
