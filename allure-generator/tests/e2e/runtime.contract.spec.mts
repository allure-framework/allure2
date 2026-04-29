import fs from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { expect, test } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { openReport, reportRoot } from "./support/report.mts";

const uiDemo = fixtures.uiDemo;
const moduleDirname = path.dirname(fileURLToPath(import.meta.url));
const generatorRoot = path.resolve(moduleDirname, "..", "..");

const collectFiles = async (directory: string): Promise<string[]> => {
  const entries = await fs.readdir(directory, { withFileTypes: true });
  const files = await Promise.all(
    entries.map(async (entry) => {
      const filePath = path.join(directory, entry.name);

      if (entry.isDirectory()) {
        return collectFiles(filePath);
      }

      return [filePath];
    }),
  );

  return files.flat();
};

const collectResultLabelGaps = async (
  directory: string,
): Promise<Array<{ file: string; missing: string[] }>> => {
  const resultFiles = (await collectFiles(directory)).filter((file) => file.endsWith("-result.json"));
  const gaps = await Promise.all(
    resultFiles.map(async (file) => {
      const content = JSON.parse(await fs.readFile(file, "utf8")) as {
        labels?: Array<{ name?: string; value?: string }>;
      };
      const labels = new Set((content.labels || []).map((label) => label.name).filter(Boolean));
      const missing = ["epic", "feature", "story", "host", "thread", "package"].filter(
        (label) => !labels.has(label),
      );
      return { file: path.basename(file), missing };
    }),
  );

  return gaps.filter(({ missing }) => missing.length > 0).sort((a, b) => a.file.localeCompare(b.file));
};

const readResultTimings = async (
  directory: string,
): Promise<Array<{ file: string; start: number; stop: number; duration: number }>> => {
  const resultFiles = (await collectFiles(directory)).filter((file) => file.endsWith("-result.json"));
  const timings = await Promise.all(
    resultFiles.map(async (file) => {
      const content = JSON.parse(await fs.readFile(file, "utf8")) as {
        start?: number;
        stop?: number;
      };
      const start = Number(content.start || 0);
      const stop = Number(content.stop || 0);
      return {
        file: path.basename(file),
        start,
        stop,
        duration: stop - start,
      };
    }),
  );

  return timings.sort((a, b) => a.file.localeCompare(b.file));
};

const hasZipMagic = (buffer: Buffer) =>
  buffer.length >= 4 &&
  buffer[0] === 0x50 &&
  buffer[1] === 0x4b &&
  [0x03, 0x05, 0x07].includes(buffer[2]) &&
  [0x04, 0x06, 0x08].includes(buffer[3]);

test.describe("Runtime Contract", () => {
  test("removes legacy globals and keeps the report scripts free of legacy package names", async ({
    page,
  }) => {
    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
    });

    await expect
      .poll(() =>
        page.evaluate(() => ({
          hasAllureGlobal: "allure" in window,
          hasJqueryGlobal: "jQuery" in window,
        })),
      )
      .toEqual({
        hasAllureGlobal: false,
        hasJqueryGlobal: false,
      });

    const assetRoot = path.join(reportRoot, uiDemo.name, REPORT_MODES.DIRECTORY, "assets");
    const bundleFiles = (await collectFiles(assetRoot)).filter((file) => file.endsWith(".js"));
    const bundle = (
      await Promise.all(bundleFiles.map((file) => fs.readFile(file, "utf8")))
    ).join("\n");

    expect(bundle).not.toContain("backbone.marionette");
    expect(bundle).not.toContain("window.jQuery");
    expect(bundle).not.toContain("window.allure");
  });

  test("preserves the existing localStorage keys for report and plugin settings", async ({ page }) => {
    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
    });

    await page.locator(".side-nav__collapse").click();
    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
      route: "suites",
    });
    await page.locator(".tree__info").click();

    await expect
      .poll(() =>
        page.evaluate(() =>
          Object.keys(window.localStorage)
            .filter((key) => key.startsWith("ALLURE_REPORT_SETTINGS"))
            .sort(),
        ),
      )
      .toEqual(["ALLURE_REPORT_SETTINGS", "ALLURE_REPORT_SETTINGS_SUITES"]);
  });

  test("preserves shell DOM hooks used by CSS customizations", async ({ page }) => {
    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
    });

    await expect(page.locator(".side-nav__brand")).toBeVisible();
    await expect(page.locator(".side-nav__brand-text")).toHaveText("Allure");
    await expect(page.locator("#content .app__nav .side-nav__menu")).toBeVisible();
  });

  test("appends the report UUID to directory-mode data requests", async ({ page }) => {
    const requests: string[] = [];

    await page.route(/\/data\/categories\.json(?:\?.*)?$/, async (route) => {
      requests.push(route.request().url());
      await route.continue();
    });

    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
      route: "categories",
    });

    await expect(page.locator('meta[name="allure-report-uuid"][content]')).toHaveCount(1);
    await expect.poll(() => requests[0] || "").toMatch(/reportUuid=/);
  });

  test("keeps the demo and raw fixtures fully labeled and timeline-friendly", async () => {
    const demoRoots = [
      path.join(generatorRoot, "test-data", "demo"),
      path.join(generatorRoot, "tests", "fixtures", "raw", uiDemo.name),
      path.join(generatorRoot, "tests", "fixtures", "raw", "attachments"),
      path.join(generatorRoot, "tests", "fixtures", "raw", "screen-diff"),
      path.join(generatorRoot, "tests", "fixtures", "raw", "playwright-trace"),
      path.join(generatorRoot, "tests", "fixtures", "raw", "commandline-smoke"),
    ];

    const fixtureChecks = (
      await Promise.all(
        demoRoots.map(async (directory) => ({
          directory: path.relative(generatorRoot, directory),
          gaps: await collectResultLabelGaps(directory),
          timings: await readResultTimings(directory),
        })),
      )
    ).map(({ directory, gaps, timings }) => {
      const starts = timings.map(({ start }) => start);
      const stops = timings.map(({ stop }) => stop);
      const tooShort = timings
        .filter(({ duration }) => duration < 4000)
        .map(({ file, duration }) => `${file}:${duration}`);
      return {
        directory,
        gaps,
        tooShort,
        span: Math.max(...stops) - Math.min(...starts),
      };
    });

    expect(
      fixtureChecks.filter(({ gaps, tooShort, span }) => gaps.length > 0 || tooShort.length > 0 || span > 10 * 60 * 1000),
    ).toEqual([]);
  });

  test("keeps Playwright trace fixtures as real zip archives", async () => {
    const traceFixtures = [
      path.join(
        generatorRoot,
        "tests",
        "fixtures",
        "raw",
        "playwright-trace",
        "playwright-trace-archive.zip",
      ),
      path.join(
        generatorRoot,
        "tests",
        "fixtures",
        "raw",
        "ui-demo",
        "playwright-trace-archive.zip",
      ),
      path.join(generatorRoot, "test-data", "demo", "playwright-trace-archive.zip"),
    ];

    const checks = await Promise.all(
      traceFixtures.map(async (file) => ({
        file: path.relative(generatorRoot, file),
        isZip: hasZipMagic(await fs.readFile(file)),
      })),
    );

    expect(checks.filter(({ isZip }) => !isZip)).toEqual([]);
  });
});
