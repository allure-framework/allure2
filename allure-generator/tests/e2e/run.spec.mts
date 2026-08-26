import { expect, test, type Page } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { openReport } from "./support/report.mts";
import { previewContainerFor } from "./support/ui.mts";

const runTabLink = (page: Page) => page.locator('.side-nav__link[data-tab="run"]');

test.describe("Run errors and attachments", () => {
  test("marks an otherwise green report unsuccessful without changing test statistics", async ({
    page,
  }) => {
    await test.step("Open the overview with a passed test and a global error", async () => {
      await openReport(page, {
        fixture: fixtures.globalsGreen.name,
        mode: REPORT_MODES.DIRECTORY,
      });

      await expect(page.locator(".run-status-banner__title")).toHaveText(
        "Run completed with errors",
      );
      await expect(page.locator(".run-status-banner__description")).toContainText(
        "All 1 reported tests passed",
      );
      await expect(page.locator(".summary-widget__stats .splash__title")).toHaveText("1");
      await expect(runTabLink(page)).toHaveClass(/side-nav__link_error/);
      await expect(runTabLink(page).locator(".side-nav__badge")).toHaveText("1");
    });

    await test.step("Review the run error and its attachment", async () => {
      await page.getByRole("link", { name: "Review run details" }).click();
      await expect(page).toHaveURL(/#run$/);
      await expect(page.locator(".run-view__outcome-title")).toHaveText("Run failed");
      await expect(page.locator(".run-view__error-message")).toHaveText(
        fixtures.globalsGreen.error,
      );
      await expect(page.locator(".run-view")).not.toContainText("Environment: qa");
      await expect(page.locator(".run-view__comparison")).toContainText("database ready");

      const attachment = page.locator(".attachment-row", {
        hasText: fixtures.globalsGreen.attachment,
      });
      await expect(attachment).toBeVisible();
      await attachment.click();
      await expect(
        previewContainerFor(attachment).locator(".attachment-preview__text"),
      ).toContainText(fixtures.globalsGreen.attachmentContent);
    });
  });

  test("explains when a run failed before any tests were reported", async ({ page }) => {
    await test.step("Open a fixture-only failure with zero test results", async () => {
      await openReport(page, {
        fixture: fixtures.globalsNoTests.name,
        mode: REPORT_MODES.DIRECTORY,
      });

      await expect(page.locator(".run-status-banner__title")).toHaveText(
        "Run failed before tests were reported",
      );
      await expect(page.locator(".summary-widget__stats .splash__title")).toHaveText("0");
      await expect(runTabLink(page).locator(".side-nav__badge")).toHaveText("1");
    });

    await test.step("Show the unmodeled fixture error without a phantom test", async () => {
      await runTabLink(page).click();
      await expect(page.locator(".run-view__error-message")).toHaveText(
        fixtures.globalsNoTests.error,
      );
      await expect(page.locator(".run-view__error")).toHaveCount(1);
      await expect(page.locator(".run-view__attachments .attachment-row")).toHaveCount(0);
    });
  });

  test("keeps attachment-only runs neutral and makes evidence available", async ({ page }) => {
    await test.step("Open an attachment-only run", async () => {
      await openReport(page, {
        fixture: fixtures.globalsAttachments.name,
        mode: REPORT_MODES.DIRECTORY,
      });

      await expect(page.locator(".run-status-banner")).toHaveCount(0);
      await expect(runTabLink(page)).toBeVisible();
      await expect(runTabLink(page)).not.toHaveClass(/side-nav__link_error/);
      await expect(runTabLink(page).locator(".side-nav__badge")).toBeHidden();
    });

    await test.step("Open the global attachment from the neutral Run page", async () => {
      await runTabLink(page).click();
      await expect(page.locator(".run-view__outcome-title")).toHaveText("No run errors");
      const attachment = page.locator(".attachment-row", {
        hasText: fixtures.globalsAttachments.attachment,
      });
      await attachment.click();
      await expect(
        previewContainerFor(attachment).locator(".attachment-preview__text"),
      ).toContainText(fixtures.globalsAttachments.attachmentContent);
    });
  });

  test("loads global metadata and attachment content from a single-file report", async ({
    page,
  }) => {
    await test.step("Open the embedded Run page", async () => {
      await openReport(page, {
        fixture: fixtures.globalsGreen.name,
        mode: REPORT_MODES.SINGLE_FILE,
        route: "run",
      });
      await expect(page.locator(".run-view__error-message")).toHaveText(
        fixtures.globalsGreen.error,
      );
    });

    await test.step("Preview the embedded run attachment", async () => {
      const attachment = page.locator(".attachment-row", {
        hasText: fixtures.globalsGreen.attachment,
      });
      await attachment.click();
      await expect(
        previewContainerFor(attachment).locator(".attachment-preview__text"),
      ).toContainText(fixtures.globalsGreen.attachmentContent);
    });
  });
});
