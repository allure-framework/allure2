import { expect, test, type Locator, type Page } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { openCaseFromTree } from "./support/report.mts";
import { stepLocator } from "./support/ui.mts";

const playwrightTrace = fixtures.playwrightTrace;

const expandStep = async (page: Page, title: string): Promise<Locator> => {
  const step = stepLocator(page, title);

  await step.locator(":scope > .step__title_hasContent").click();
  await expect(step).toHaveClass(/step_expanded/);
  return step;
};

test.describe("Playwright Trace", () => {
  test("uses the trace-specific attachment flow and mounts the trace viewer iframe", async ({
    page,
  }) => {
    await openCaseFromTree(page, {
      fixture: playwrightTrace.name,
      mode: REPORT_MODES.DIRECTORY,
      tab: "suites",
      caseName: playwrightTrace.caseName,
    });

    const traceRow = (await expandStep(page, playwrightTrace.stepName)).locator(".attachment-row", {
      hasText: playwrightTrace.attachmentName,
    });
    await expect(traceRow).toBeVisible();
    await expect(traceRow).toHaveAttribute("data-type", "application/vnd.allure.playwright-trace");

    await traceRow.click();
    await expect(page.locator(".attachment__trace-container")).toHaveCount(0);

    await traceRow.locator(".attachment-row__fullscreen").click();
    await expect(page).toHaveURL(/attachment=/);

    const traceFrame = page.locator("#pw-trace-iframe");
    await expect(traceFrame).toBeVisible();
    await expect(traceFrame).toHaveAttribute("src", /trace\.playwright\.dev\/next/);
    await expect
      .poll(() =>
        page.evaluate(() => {
          const iframe = document.getElementById("pw-trace-iframe");
          return typeof iframe?.onload === "function";
        }),
      )
      .toBe(true);
  });
});
