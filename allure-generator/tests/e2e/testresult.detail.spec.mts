import { expect, test } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { openCaseFromTree } from "./support/report.mts";
import { stepLocator } from "./support/ui.mts";

const uiDemo = fixtures.uiDemo;
const reportModes = [REPORT_MODES.SINGLE_FILE, REPORT_MODES.DIRECTORY] as const;

for (const mode of reportModes) {
  test.describe(`Test Result Detail (${mode})`, () => {
    test("renders metadata, overview blocks, and execution content", async ({ page }) => {
      await openCaseFromTree(page, {
        fixture: uiDemo.name,
        mode,
        tab: "suites",
        caseName: uiDemo.cases.failedPullRequest,
      });

      await expect(page.locator(".test-result__status .label_status_failed")).toHaveText("Failed");
      await expect(page.locator(".test-result__name")).toContainText(
        uiDemo.cases.failedPullRequest,
      );
      await expect(page.locator(".fullname__body")).toHaveText(
        "io.qameta.allure.PullRequestsWebTest.shouldCreatePullRequest",
      );

      const overview = page.locator(".test-result-overview");
      await expect(overview).toContainText("Tags:");
      await expect(overview).toContainText("web");
      await expect(overview).toContainText("smoke");
      await expect(overview).toContainText("Categories:");
      await expect(overview).toContainText("Product defects");
      await expect(overview).toContainText("Severity:");
      await expect(overview).toContainText("normal");
      await expect(overview).toContainText("Duration:");
      await expect(overview).toContainText("Owner");
      await expect(overview).toContainText("eroshenkoam");

      const execution = page.locator(".test-result-execution");
      await expect(execution).toContainText("Set up");
      await expect(execution).toContainText("Test body");
      await expect(execution).toContainText("Tear down");
      await expect(execution).toContainText("Open pull requests page");
    });

    test("updates attachment routing for modal previews", async ({ page }) => {
      await openCaseFromTree(page, {
        fixture: uiDemo.name,
        mode,
        tab: "suites",
        caseName: uiDemo.cases.passedPullRequest,
      });

      const attachmentStep = stepLocator(page, uiDemo.htmlAttachmentStep);
      await attachmentStep.locator(".step__name").first().click();
      await expect(attachmentStep).toHaveClass(/step_expanded/);

      const attachmentRow = attachmentStep
        .locator(".attachment-row", { hasText: uiDemo.htmlAttachmentName })
        .first();
      await expect(attachmentRow).toBeVisible();
      await expect(attachmentRow.getByRole("img", { name: "text/html" })).toBeVisible();
      await attachmentRow.locator(".attachment-row__fullscreen").click();

      await expect(page).toHaveURL(/attachment=/);
      await expect(page.locator("#content")).toHaveClass(/blur/);
      await expect(page.locator(".attachment__iframe")).toBeVisible();

      await page.keyboard.press("Escape");
      await expect(page).not.toHaveURL(/attachment=/);
      await expect(page.locator("#content")).not.toHaveClass(/blur/);
      await expect(page.locator(".attachment__iframe")).toHaveCount(0);
    });

    test("renders history and retries for retried results", async ({ page }) => {
      await openCaseFromTree(page, {
        fixture: uiDemo.name,
        mode,
        tab: "suites",
        caseName: uiDemo.cases.failedPullRequest,
      });

      await page.getByRole("link", { name: "History" }).click();
      await expect(page.locator(".tab_active")).toContainText("History");
      await expect(page.locator(".test-result-history__success-rate")).toContainText(
        "Success rate",
      );
      await expect(page.locator(".test-result-history__success-rate")).toContainText("(14 of 29)");
      await expect(page.locator(".test-result-history .label_status_failed").first()).toBeVisible();

      await page.getByRole("link", { name: "Retries" }).click();
      await expect(page.locator(".tab_active")).toContainText("Retries");
      await expect(page.locator(".pane__section .label_status_failed").first()).toBeVisible();
      await expect(page.locator(".preformated-text code").first()).toContainText(
        "Element not found",
      );
    });

    test("shows clipboard tooltip feedback for fullname copy", async ({ page }) => {
      await openCaseFromTree(page, {
        fixture: uiDemo.name,
        mode,
        tab: "suites",
        caseName: uiDemo.cases.failedPullRequest,
      });

      const copyButton = page.locator(".fullname__copy");
      await copyButton.hover();
      await expect(page.locator(".tooltip.tooltip_position_left")).toHaveText("Copy to clipboard");

      await page.evaluate(() => {
        document.execCommand = () => true;
      });
      await copyButton.click();
      await expect(page.locator(".tooltip.tooltip_position_left")).toHaveText("Successfully copied");

      await page.evaluate(() => {
        document.execCommand = () => false;
      });
      await copyButton.click();
      await expect(page.locator(".tooltip.tooltip_position_left")).toContainText(
        "Can not copy value to clipboard",
      );
    });

    test("renders html-like status details as plain text", async ({ page }) => {
      await openCaseFromTree(page, {
        fixture: uiDemo.name,
        mode,
        tab: "suites",
        caseName: uiDemo.cases.statusDetailsHtmlTags,
      });

      const overview = page.locator(".test-result-overview");
      const statusDetails = overview.locator(".status-details");

      await expect(overview).toBeVisible();
      await expect(overview.locator(".test-result-overview__execution")).toBeVisible();
      await expect(statusDetails.locator(".status-details__message code")).toHaveText(
        uiDemo.expectedStatusDetailsHtmlTags,
      );
      await expect(statusDetails.locator("input, textarea, select")).toHaveCount(0);

      await statusDetails.locator(".status-details__trace-toggle").click();

      await expect(statusDetails.locator(".status-details__trace code")).toHaveText(
        uiDemo.expectedStatusDetailsHtmlTags,
      );
      await expect(statusDetails.locator("input, textarea, select")).toHaveCount(0);
    });
  });
}
