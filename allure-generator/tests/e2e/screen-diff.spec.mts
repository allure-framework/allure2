import { expect, test } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { openCaseFromTree } from "./support/report.mts";
import { previewContainerFor, stepLocator } from "./support/ui.mts";

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

    await expect(
      page.locator('.attachment-row[data-type="application/vnd.allure.image.diff"]'),
    ).toHaveCount(3);

    const checkoutStep = stepLocator(page, screenDiff.steps.checkout);
    await checkoutStep.locator(".step__name").first().click();
    await expect(checkoutStep).toHaveClass(/step_expanded/);

    const checkoutRow = checkoutStep.locator(".attachment-row", {
      hasText: screenDiff.attachments.checkout,
    });
    await expect(checkoutRow).toBeVisible();
    await checkoutRow.click();

    const checkoutPreview = previewContainerFor(checkoutRow);
    await expect(checkoutPreview.locator(".screen-diff__content")).toBeVisible();
    await expect(
      checkoutPreview.locator('.screen-diff__switchers input[value="overlay"]'),
    ).toBeChecked();
    await expect(checkoutPreview.locator(".screen-diff__overlay")).toBeVisible();
    await checkoutPreview.locator('.screen-diff__switchers input[value="diff"]').check();
    await expect(
      checkoutPreview.locator('.screen-diff__switchers input[value="diff"]'),
    ).toBeChecked();
  });
});
