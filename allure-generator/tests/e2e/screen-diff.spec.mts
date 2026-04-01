import { expect, test } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { openCaseFromTree } from "./support/report.mts";

const screenDiff = fixtures.screenDiff;

test.describe("Screen Diff", () => {
  test("renders the dedicated screen-diff block and persists the selected mode", async ({
    page,
  }) => {
    await openCaseFromTree(page, {
      fixture: screenDiff.name,
      mode: REPORT_MODES.DIRECTORY,
      tab: "suites",
      caseName: screenDiff.caseName,
    });

    await expect(
      page.locator(".pane__section-title", { hasText: screenDiff.blockTitle }),
    ).toBeVisible();
    await expect(page.locator('.screen-diff__switchers input[value="diff"]')).toBeChecked();

    await page.locator('.screen-diff__switchers input[value="overlay"]').check();
    await expect(page.locator(".screen-diff__overlay")).toBeVisible();

    await page.reload();
    await expect(page.locator(".test-result__name")).toContainText(screenDiff.caseName);
    await expect(
      page.locator(".pane__section-title", { hasText: screenDiff.blockTitle }),
    ).toBeVisible();
    await expect(page.locator('.screen-diff__switchers input[value="overlay"]')).toBeChecked();
    await expect(page.locator(".screen-diff__overlay")).toBeVisible();
  });
});
