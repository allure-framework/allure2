import { expect, test, type Page } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { openCaseFromTree, openReport } from "./support/report.mts";

const uiDemo = fixtures.uiDemo;
const attachmentsFixture = fixtures.attachments;

const attachmentRow = (page: Page, name: string) =>
  page.locator(".attachment-row", {
    hasText: name,
  });

const settlePage = async (page: Page): Promise<void> => {
  await page.evaluate(async () => {
    if (document.fonts?.ready) {
      await document.fonts.ready;
    }

    const waitForLoaders = async () => {
      const startedAt = Date.now();

      while (document.querySelector(".loader__mask")) {
        if (Date.now() - startedAt > 5000) {
          break;
        }

        await new Promise((resolve) => window.setTimeout(resolve, 100));
      }
    };

    await waitForLoaders();
    await new Promise((resolve) => window.setTimeout(resolve, 300));
  });
};

test.describe("Visual Parity", () => {
  test("captures the overview dashboard", async ({ page }) => {
    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.SINGLE_FILE,
    });

    await settlePage(page);
    await expect(page.locator(".app")).toHaveScreenshot("overview-dashboard.png");
  });

  test("captures the suites tree with a selected result", async ({ page }) => {
    await openCaseFromTree(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
      tab: "suites",
      caseName: uiDemo.cases.failedPullRequest,
    });

    await settlePage(page);
    await expect(page.locator(".app")).toHaveScreenshot("suites-selected-result.png");
  });

  test("captures the graph dashboard", async ({ page }) => {
    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
      route: "graph",
    });

    await settlePage(page);
    await expect(page.locator(".app")).toHaveScreenshot("graph-dashboard.png");
  });

  test("captures the test result details surface", async ({ page }) => {
    await openCaseFromTree(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.SINGLE_FILE,
      tab: "suites",
      caseName: uiDemo.cases.failedPullRequest,
    });

    await settlePage(page);
    await expect(page.locator(".app")).toHaveScreenshot("testresult-details.png");
  });

  test("captures the fullscreen attachment modal", async ({ page }) => {
    await openCaseFromTree(page, {
      fixture: attachmentsFixture.name,
      mode: REPORT_MODES.DIRECTORY,
      tab: "suites",
      caseName: attachmentsFixture.caseName,
    });

    const pngRow = attachmentRow(page, attachmentsFixture.attachments.png);
    await expect(pngRow).toBeVisible();
    await pngRow.locator(".attachment-row__fullscreen").click();

    await settlePage(page);
    await expect(page.locator(".modal__content")).toHaveScreenshot("attachment-modal.png");
  });
});
