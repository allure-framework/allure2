import { readdir, readFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const translationsDir = path.resolve(__dirname, "../src/main/javascript/translations");
const sourceLanguage = "en.json";
const interpolationPattern = /{{\s*([^{}\s]+)\s*}}/g;
const localePluralSuffixPattern = /^(.*)_(zero|one|two|few|many|other|\d+)$/;

const collectEntries = (value, prefix = "", entries = new Map()) => {
  if (!value || typeof value !== "object" || Array.isArray(value)) {
    throw new Error(`Expected object at "${prefix || "<root>"}"`);
  }

  for (const [key, child] of Object.entries(value)) {
    const currentPath = prefix ? `${prefix}.${key}` : key;
    if (typeof child === "string") {
      entries.set(currentPath, {
        kind: "string",
        placeholders: [...child.matchAll(interpolationPattern)].map((match) => match[1]).sort(),
      });
      continue;
    }

    if (!child || typeof child !== "object" || Array.isArray(child)) {
      throw new Error(`Expected object or string at "${currentPath}"`);
    }

    entries.set(currentPath, { kind: "object" });
    collectEntries(child, currentPath, entries);
  }

  return entries;
};

const formatList = (items) => items.map((item) => `  - ${item}`).join("\n");

const placeholderList = (items) => items.map((item) => `{{${item}}}`).join(", ");

const isAllowedPluralVariant = (translationPath, sourceEntries) => {
  const match = translationPath.match(localePluralSuffixPattern);
  if (!match) {
    return false;
  }

  const basePath = match[1];
  return (
    sourceEntries.has(basePath) ||
    sourceEntries.has(`${basePath}_plural`) ||
    sourceEntries.has(`${basePath}_one`) ||
    sourceEntries.has(`${basePath}_other`)
  );
};

const main = async () => {
  const files = (await readdir(translationsDir))
    .filter((fileName) => fileName.endsWith(".json"))
    .sort();
  const sourceBundle = JSON.parse(await readFile(path.join(translationsDir, sourceLanguage), "utf8"));
  const sourceEntries = collectEntries(sourceBundle);
  const failures = [];

  for (const fileName of files) {
    const localeBundle = JSON.parse(await readFile(path.join(translationsDir, fileName), "utf8"));
    const localeEntries = collectEntries(localeBundle);
    const missingPaths = [];
    const extraPaths = [];
    const placeholderMismatches = [];
    const shapeMismatches = [];

    for (const [translationPath, sourceEntry] of sourceEntries.entries()) {
      const localeEntry = localeEntries.get(translationPath);
      if (!localeEntry) {
        missingPaths.push(translationPath);
        continue;
      }

      if (localeEntry.kind !== sourceEntry.kind) {
        shapeMismatches.push(
          `${translationPath} (${sourceEntry.kind} in ${sourceLanguage}, ${localeEntry.kind} in ${fileName})`,
        );
        continue;
      }

      if (sourceEntry.kind === "string") {
        const sourcePlaceholders = sourceEntry.placeholders.join("|");
        const localePlaceholders = localeEntry.placeholders.join("|");
        if (sourcePlaceholders !== localePlaceholders) {
          placeholderMismatches.push(
            `${translationPath} (${sourceLanguage}: ${placeholderList(sourceEntry.placeholders)}; ${fileName}: ${placeholderList(localeEntry.placeholders)})`,
          );
        }
      }
    }

    for (const translationPath of localeEntries.keys()) {
      if (!sourceEntries.has(translationPath) && !isAllowedPluralVariant(translationPath, sourceEntries)) {
        extraPaths.push(translationPath);
      }
    }

    if (missingPaths.length || extraPaths.length || placeholderMismatches.length || shapeMismatches.length) {
      failures.push([
        `${fileName}:`,
        missingPaths.length ? `Missing keys:\n${formatList(missingPaths)}` : "",
        extraPaths.length ? `Extra keys:\n${formatList(extraPaths)}` : "",
        shapeMismatches.length ? `Shape mismatches:\n${formatList(shapeMismatches)}` : "",
        placeholderMismatches.length
          ? `Interpolation mismatches:\n${formatList(placeholderMismatches)}`
          : "",
      ].filter(Boolean).join("\n"));
    }
  }

  if (failures.length) {
    console.error("Translation validation failed:\n");
    console.error(failures.join("\n\n"));
    process.exitCode = 1;
    return;
  }

  console.log(`Validated ${files.length} translation bundles against ${sourceLanguage}.`);
};

await main();
