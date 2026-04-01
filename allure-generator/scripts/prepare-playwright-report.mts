import { execFileSync } from "node:child_process";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const moduleFilename = fileURLToPath(import.meta.url);
const moduleDirname = path.dirname(moduleFilename);
const generatorRoot = path.resolve(moduleDirname, "..");
const repoRoot = path.resolve(generatorRoot, "..");

export const e2eRoot = path.join(generatorRoot, "build", "e2e");
export const REPORT_MODES = {
  SINGLE_FILE: "single-file",
  DIRECTORY: "directory",
} as const;

export type ReportMode = (typeof REPORT_MODES)[keyof typeof REPORT_MODES];

export interface ReportRequest {
  fixture: string;
  mode: ReportMode;
}

export const DEFAULT_REPORTS: ReportRequest[] = [
  { fixture: "new-demo", mode: REPORT_MODES.SINGLE_FILE },
  { fixture: "new-demo", mode: REPORT_MODES.DIRECTORY },
  { fixture: "allure2", mode: REPORT_MODES.DIRECTORY },
  { fixture: "screen-diff", mode: REPORT_MODES.DIRECTORY },
  { fixture: "playwright-trace", mode: REPORT_MODES.DIRECTORY },
];

const gradleWrapper = path.join(repoRoot, process.platform === "win32" ? "gradlew.bat" : "gradlew");
const allureBinary = path.join(
  repoRoot,
  "allure-commandline",
  "build",
  "install",
  "allure-commandline",
  "bin",
  process.platform === "win32" ? "allure.bat" : "allure",
);

const run = (command: string, args: string[], cwd: string): void => {
  execFileSync(command, args, {
    cwd,
    stdio: "inherit",
  });
};

const ensureAllureCommandline = (): void => {
  if (fs.existsSync(allureBinary)) {
    return;
  }

  run(gradleWrapper, [":allure-commandline:build"], repoRoot);
};

const ensureMode = (mode: string): ReportMode => {
  if (!Object.values(REPORT_MODES).includes(mode as ReportMode)) {
    throw new Error(`Unsupported report mode "${mode}"`);
  }

  return mode as ReportMode;
};

export const getFixtureInputDir = (fixture: string): string =>
  path.join(generatorRoot, "test-data", fixture);

export const getReportOutputDir = ({ fixture, mode }: ReportRequest): string =>
  path.join(e2eRoot, fixture, mode);

export const getReportIndexPath = ({ fixture, mode }: ReportRequest): string =>
  path.join(getReportOutputDir({ fixture, mode }), "index.html");

export const prepareSingleReport = ({ fixture, mode }: ReportRequest): string => {
  const ensuredMode = ensureMode(mode);
  const inputDir = getFixtureInputDir(fixture);
  if (!fs.existsSync(inputDir)) {
    throw new Error(`Fixture "${fixture}" does not exist at ${inputDir}`);
  }

  const outputDir = path.relative(
    generatorRoot,
    getReportOutputDir({ fixture, mode: ensuredMode }),
  );
  const inputPath = path.relative(generatorRoot, inputDir);
  const args = ["generate", inputPath, "-o", outputDir, "--clean"];
  if (ensuredMode === REPORT_MODES.SINGLE_FILE) {
    args.push("--single-file");
  }

  run(allureBinary, args, generatorRoot);
  return getReportIndexPath({ fixture, mode: ensuredMode });
};

const parseCliReports = (argv: string[]): ReportRequest[] => {
  const args: { fixture: string | null; mode: string | null } = {
    fixture: null,
    mode: null,
  };

  argv.forEach((arg) => {
    if (arg.startsWith("--fixture=")) {
      args.fixture = arg.slice("--fixture=".length);
    } else if (arg === "--fixture") {
      args.fixture = "";
    } else if (arg.startsWith("--mode=")) {
      args.mode = arg.slice("--mode=".length);
    } else if (arg === "--mode") {
      args.mode = "";
    }
  });

  const fixtureIndex = argv.indexOf("--fixture");
  if (fixtureIndex !== -1 && argv[fixtureIndex + 1]) {
    args.fixture = argv[fixtureIndex + 1];
  }

  const modeIndex = argv.indexOf("--mode");
  if (modeIndex !== -1 && argv[modeIndex + 1]) {
    args.mode = argv[modeIndex + 1];
  }

  if (!args.fixture && !args.mode) {
    return DEFAULT_REPORTS;
  }

  if (!args.fixture || !args.mode) {
    throw new Error("Both --fixture and --mode are required when preparing a single report");
  }

  return [{ fixture: args.fixture, mode: ensureMode(args.mode) }];
};

export default async function preparePlaywrightReport({
  reports = DEFAULT_REPORTS,
}: { reports?: ReportRequest[] } = {}): Promise<string[]> {
  ensureAllureCommandline();
  return reports.map(prepareSingleReport);
}

if (process.argv[1] && path.resolve(process.argv[1]) === moduleFilename) {
  void preparePlaywrightReport({ reports: parseCliReports(process.argv.slice(2)) }).catch(
    (error: unknown) => {
      const message = error instanceof Error ? error.stack ?? error.message : String(error);
      process.stderr.write(`${message}\n`);
      process.exit(1);
    },
  );
}
