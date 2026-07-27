import { expect, test } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { openCaseFromTree } from "./support/report.mts";
import { stepLocator } from "./support/ui.mts";

const uiDemo = fixtures.uiDemo;
const detectedLinks = fixtures.detectedLinks;
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

    test("links only whole absolute parameter values", async ({ page }) => {
      await openCaseFromTree(page, {
        fixture: detectedLinks.name,
        mode,
        tab: "suites",
        caseName: detectedLinks.caseName,
      });

      const absoluteUrl = page.locator(".environment", {
        hasText: "Absolute URL: https://example.org/docs",
      });
      await expect(
        absoluteUrl.getByRole("link", { name: "https://example.org/docs" }),
      ).toHaveAttribute("href", "https://example.org/docs");

      const wwwUrl = page.locator(".environment", {
        hasText: "WWW URL: www.example.org/docs",
      });
      await expect(wwwUrl.getByRole("link", { name: "www.example.org/docs" })).toHaveAttribute(
        "href",
        "https://www.example.org/docs",
      );

      const encodedWwwUrl = page.locator(".environment", {
        hasText: "Encoded WWW URL: &#119;ww.example.org",
      });
      await expect(encodedWwwUrl.getByRole("link")).toHaveCount(0);

      await expect(
        page
          .locator(".environment", { hasText: "Relative URL: ./index.html#/graphs" })
          .getByRole("link"),
      ).toHaveCount(0);
      await expect(
        page
          .locator(".environment", { hasText: "Protocol-relative URL: //example.org/docs" })
          .getByRole("link"),
      ).toHaveCount(0);
      await expect(
        page.locator(".environment", { hasText: "Plain text: API - Local" }).getByRole("link"),
      ).toHaveCount(0);
      await expect(
        page
          .locator(".environment", { hasText: "Embedded URL: Open https://example.org/docs" })
          .getByRole("link"),
      ).toHaveCount(0);
      await expect(
        page
          .locator(".environment", { hasText: "Dangerous URL: javascript:alert(1)" })
          .getByRole("link"),
      ).toHaveCount(0);
    });

    test("detects only absolute links in step names", async ({ page }) => {
      await openCaseFromTree(page, {
        fixture: detectedLinks.name,
        mode,
        tab: "suites",
        caseName: detectedLinks.caseName,
      });

      const stepName = page.locator(".step__name", {
        hasText: "Open www.example.org/docs and compare ./index.html#/graphs",
      });
      await expect(stepName.getByRole("link", { name: "www.example.org/docs" })).toHaveAttribute(
        "href",
        "https://www.example.org/docs",
      );
      await expect(stepName.getByRole("link")).toHaveCount(1);
      await expect(stepName).toContainText("./index.html#/graphs");

      const entityEncodedUrl = page.locator(".step__name", {
        hasText: "Open https://a.org/x?a=1&amp;b=2",
      });
      await expect(
        entityEncodedUrl.getByRole("link", { name: "https://a.org/x?a=1&amp;b=2" }),
      ).toHaveAttribute("href", "https://a.org/x?a=1&amp;b=2");

      const dangerousScheme = page.locator(".step__name", {
        hasText: "Do not open javascript:alert(1)",
      });
      await expect(dangerousScheme).toContainText("javascript:alert(1)");
      await expect(dangerousScheme.getByRole("link")).toHaveCount(0);
    });

    test("uses only Link.url as an explicit link target", async ({ page }) => {
      await openCaseFromTree(page, {
        fixture: detectedLinks.name,
        mode,
        tab: "suites",
        caseName: detectedLinks.caseName,
      });

      const relativeLink = page.locator(".testresult-link", { hasText: "Relative report" });
      await expect(relativeLink.getByRole("link", { name: "Relative report" })).toHaveAttribute(
        "href",
        "reports/latest/index.html",
      );

      const labelOnly = page.locator(".testresult-link", { hasText: "Label without URL" });
      await expect(labelOnly).toContainText("Label without URL");
      await expect(labelOnly.getByRole("link")).toHaveCount(0);
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
      await expect(page.locator(".attachment-preview__frame")).toBeVisible();

      await page.keyboard.press("Escape");
      await expect(page).not.toHaveURL(/attachment=/);
      await expect(page.locator("#content")).not.toHaveClass(/blur/);
      await expect(page.locator(".attachment-preview__frame")).toHaveCount(0);
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
