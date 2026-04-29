import { expect, test, type Locator, type Page } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { openCaseFromTree } from "./support/report.mts";
import { previewContainerFor, stepLocator } from "./support/ui.mts";

const playwrightTrace = fixtures.playwrightTrace;
const reportModes = [REPORT_MODES.SINGLE_FILE, REPORT_MODES.DIRECTORY] as const;

const expandStep = async (page: Page, title: string): Promise<Locator> => {
  const step = stepLocator(page, title);

  await step.locator(".step__name").first().click();
  await expect(step).toHaveClass(/step_expanded/);
  return step;
};

for (const mode of reportModes) {
  test.describe(`Playwright Trace (${mode})`, () => {
    test("opens the trace in the modal and keeps a download fallback visible", async ({
      page,
    }) => {
      await openCaseFromTree(page, {
        fixture: playwrightTrace.name,
        mode,
        tab: "suites",
        caseName: playwrightTrace.caseName,
      });

      const traceRow = (await expandStep(page, playwrightTrace.stepName)).locator(".attachment-row", {
        hasText: playwrightTrace.attachmentName,
      });
      await expect(traceRow).toBeVisible();
      await expect(traceRow).toHaveAttribute(
        "data-type",
        "application/vnd.allure.playwright-trace",
      );

      await traceRow.click();
      await expect(page).toHaveURL(/attachment=/);
      await expect(traceRow).not.toHaveClass(/attachment-row_selected/);
      await expect(previewContainerFor(traceRow).locator("#pw-trace-iframe")).toHaveCount(0);

      const traceFrame = page.locator(".modal__content #pw-trace-iframe");
      const downloadLink = page.locator(".modal__content .attachment__trace-download");
      await expect(traceFrame).toBeVisible();
      await expect(traceFrame).toHaveAttribute("src", "https://trace.playwright.dev/");
      await expect(downloadLink).toBeVisible();
      await expect(downloadLink).toHaveAttribute("download", /\.zip$/);
      await expect(downloadLink).toHaveAttribute("href", /^blob:/);
    });

    test("loads the trace content on the first modal open", async ({ page }) => {
      await openCaseFromTree(page, {
        fixture: playwrightTrace.name,
        mode,
        tab: "suites",
        caseName: playwrightTrace.caseName,
      });

      const traceRow = (await expandStep(page, playwrightTrace.stepName)).locator(".attachment-row", {
        hasText: playwrightTrace.attachmentName,
      });
      await traceRow.click();

      const traceFrame = page.frameLocator(".modal__content #pw-trace-iframe");
      await expect(traceFrame.getByRole("tab", { name: "Action", exact: true })).toBeVisible();
    });
  });
}
