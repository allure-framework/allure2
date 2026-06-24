import { expect, test, type Locator, type Page } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { openCaseFromTree } from "./support/report.mts";
import { previewContainerFor, stepLocator } from "./support/ui.mts";

const playwrightTrace = fixtures.playwrightTrace;

const expandStep = async (page: Page, title: string): Promise<Locator> => {
  const step = stepLocator(page, title);

  await step.locator(".step__name").first().click();
  await expect(step).toHaveClass(/step_expanded/);
  return step;
};

test.describe("Playwright Trace", () => {
  for (const mode of [REPORT_MODES.SINGLE_FILE, REPORT_MODES.DIRECTORY] as const) {
    test(`opens the trace modal and keeps a download fallback visible (${mode})`, async ({
      page,
    }) => {
      await openCaseFromTree(page, {
        fixture: playwrightTrace.name,
        mode,
        tab: "suites",
        caseName: playwrightTrace.caseName,
      });

      const traceRow = (await expandStep(page, playwrightTrace.stepName)).locator(
        ".attachment-row",
        {
          hasText: playwrightTrace.attachmentName,
        },
      );
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
      const downloadLink = page.locator(".modal__title .attachment-preview__trace-download");
      await expect(downloadLink).toBeVisible();
      await expect(downloadLink).toHaveAttribute("download", /\.zip$/);

      if (mode === REPORT_MODES.DIRECTORY) {
        await expect(traceFrame).toBeVisible();
        await expect(traceFrame).toHaveAttribute(
          "src",
          /\/playwright-trace-viewer\/index\.html\?trace=/,
        );

        const iframeSrc = await traceFrame.getAttribute("src");
        const traceUrl = new URL(iframeSrc ?? "").searchParams.get("trace");
        expect(traceUrl).toContain("/data/attachments/");
        expect(traceUrl).toContain("reportUuid=");
        await expect(downloadLink).toHaveAttribute("href", /\/data\/attachments\/.*reportUuid=/);
      } else {
        await expect(downloadLink).toHaveAttribute("href", /^blob:/);
        await expect(traceFrame).toHaveCount(0);
        const instructions = page.locator(".modal__content .attachment-preview__trace-instructions");
        await expect(instructions).toContainText("single-file reports");
        await expect(instructions).toContainText("Open the trace manually");
        await expect(instructions.getByRole("link", { name: "microsoft/playwright#40960" }))
          .toHaveAttribute("href", "https://github.com/microsoft/playwright/issues/40960");
        await expect(instructions.getByRole("link", { name: "Download the trace archive" }))
          .toHaveAttribute("download", /\.zip$/);
        await expect(
          instructions.getByRole("link", { name: "Open the Playwright Trace Viewer" }),
        ).toHaveAttribute("href", "https://trace.playwright.dev/");
        await expect(instructions).toContainText(
          "Upload the downloaded archive in the viewer",
        );
      }
    });
  }

  test("loads the trace content on the first modal open in directory reports", async ({ page }) => {
    await openCaseFromTree(page, {
      fixture: playwrightTrace.name,
      mode: REPORT_MODES.DIRECTORY,
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

  test("loads the trace through the viewer URL without message handoff", async ({ page }) => {
    await page.route(/\/playwright-trace-viewer\/index\.[^/]+\.js(?:\?.*)?$/, async (route) => {
      const response = await route.fetch();
      const source = await response.text();
      const disabledMessageListener = `
        const __allureTraceViewerAddEventListener = window.addEventListener.bind(window);
        window.addEventListener = function (type, listener, options) {
          if (type === "message") {
            return;
          }
          return __allureTraceViewerAddEventListener(type, listener, options);
        };
      `;

      await route.fulfill({
        response,
        body: `${disabledMessageListener}\n${source}`,
      });
    });

    await openCaseFromTree(page, {
      fixture: playwrightTrace.name,
      mode: REPORT_MODES.DIRECTORY,
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
