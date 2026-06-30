import { expect, test, type Locator, type Page } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { directoryServerOrigin, openCaseFromTree } from "./support/report.mts";
import { previewContainerFor, stepLocator } from "./support/ui.mts";

const playwrightTrace = fixtures.playwrightTrace;
const ATTACHMENT_PREVIEW_MAX_BYTES = 10 * 1024 * 1024;
const TRACE_VIEWER_URL = "https://trace.playwright.dev/";

// Mirror of LocalReportServer.REPORT_CONTENT_SECURITY_POLICY sent by `allure open`/
// `allure serve`: script-src allows data: (so single-file reports, which inline their
// bundle via <script src="data:...">, boot) but connect-src stays 'self' (so embedded
// attachments must be decoded without fetch).
const LOCAL_SERVER_REPORT_CSP =
  "default-src 'self'; object-src 'none'; base-uri 'none'; form-action 'none'; " +
  "frame-ancestors 'none'; img-src 'self' data: blob: https:; media-src 'self' data: blob: https:; " +
  "font-src 'self' data: https:; connect-src 'self'; " +
  "frame-src 'self' blob: https://trace.playwright.dev; worker-src 'self' blob:; " +
  "script-src 'self' 'unsafe-inline' https: data:; style-src 'self' 'unsafe-inline' https:";

// A hermetic stand-in for the hosted Playwright Trace Viewer. It records the
// trace handed over through postMessage so the tests can assert the handoff
// without depending on the live external service. The message listener can be
// attached after a delay to emulate the real viewer, which registers it from a
// React passive effect that runs after the iframe load event.
const traceViewerStub = (listenerDelayMs: number) => `<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8" />
    <title>Playwright Trace Viewer stub</title>
    <script>
      const attachListener = () => {
        window.addEventListener("message", (event) => {
          const data = event.data || {};
          const trace = data.params && data.params.trace;
          if (data.method === "load" && trace instanceof Blob) {
            const root = document.documentElement;
            root.setAttribute("data-trace-loaded", "1");
            root.setAttribute("data-trace-size", String(trace.size));
          }
        });
      };

      if (${listenerDelayMs} > 0) {
        window.setTimeout(attachListener, ${listenerDelayMs});
      } else {
        attachListener();
      }
    </script>
  </head>
  <body>
    <main data-stub-viewer>Playwright Trace Viewer stub</main>
  </body>
</html>`;

const mockTraceViewer = async (page: Page, listenerDelayMs = 400): Promise<void> => {
  await page.route("https://trace.playwright.dev/**", (route) =>
    route.fulfill({
      contentType: "text/html; charset=utf-8",
      body: traceViewerStub(listenerDelayMs),
    }),
  );
};

const expandStep = async (page: Page, title: string): Promise<Locator> => {
  const step = stepLocator(page, title);

  await step.locator(".step__name").first().click();
  await expect(step).toHaveClass(/step_expanded/);
  return step;
};

test.describe("Playwright Trace", () => {
  for (const mode of [REPORT_MODES.SINGLE_FILE, REPORT_MODES.DIRECTORY] as const) {
    test(`opens the trace viewer and hands the trace over via postMessage (${mode})`, async ({
      page,
    }) => {
      await mockTraceViewer(page);

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
      await expect(traceFrame).toBeVisible();
      await expect(traceFrame).toHaveAttribute("src", TRACE_VIEWER_URL);

      const downloadLink = page.locator(".modal__title .attachment-preview__trace-download");
      await expect(downloadLink).toBeVisible();
      await expect(downloadLink).toHaveAttribute("download", /\.zip$/);
      await expect(downloadLink).toHaveAttribute("href", /^blob:/);

      const viewerRoot = page.frameLocator(".modal__content #pw-trace-iframe").locator("html");
      await expect(viewerRoot).toHaveAttribute("data-trace-loaded", "1");
      await expect(viewerRoot).toHaveAttribute("data-trace-size", /^[1-9][0-9]*$/);
    });
  }

  test("re-sends the trace until the viewer attaches its listener", async ({ page }) => {
    // The listener only appears well after the iframe load event (and after the
    // first couple of handoff attempts), so the trace shows up only if the view
    // keeps re-posting it.
    await mockTraceViewer(page, 800);

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

    const viewerRoot = page.frameLocator(".modal__content #pw-trace-iframe").locator("html");
    await expect(viewerRoot).toHaveAttribute("data-trace-loaded", "1");
  });

  test("loads single-file traces under the allure serve content security policy", async ({
    page,
  }) => {
    await mockTraceViewer(page);

    const singleFileUrl = `${directoryServerOrigin}/${playwrightTrace.name}/${REPORT_MODES.SINGLE_FILE}/index.html`;

    // Reproduce the local report server response for single-file reports: the
    // document is served with the relaxed-script-src / strict-connect-src CSP.
    // It boots only if data: scripts are allowed, and the trace loads only if
    // embedded data: attachments are decoded instead of fetched.
    await page.route(
      `**/${playwrightTrace.name}/${REPORT_MODES.SINGLE_FILE}/index.html`,
      async (route) => {
        const response = await route.fetch();
        await route.fulfill({
          response,
          headers: { ...response.headers(), "content-security-policy": LOCAL_SERVER_REPORT_CSP },
        });
      },
    );

    await page.goto(`${singleFileUrl}#suites`, { waitUntil: "domcontentloaded" });
    const searchInput = page.locator(".search__input");
    await expect(searchInput).toBeVisible();
    await searchInput.fill(playwrightTrace.caseName);

    const leaf = page
      .locator(".node__leaf:visible")
      .filter({ hasText: playwrightTrace.caseName })
      .first();
    for (let index = 0; index < 10; index += 1) {
      if (await leaf.isVisible().catch(() => false)) {
        break;
      }
      const collapsedGroupTitle = page
        .locator(".node[data-node-kind='group']:visible:not(.node__expanded) > .node__title")
        .first();
      if (!(await collapsedGroupTitle.isVisible().catch(() => false))) {
        break;
      }
      await collapsedGroupTitle.click();
    }
    await leaf.click();

    const traceRow = (await expandStep(page, playwrightTrace.stepName)).locator(".attachment-row", {
      hasText: playwrightTrace.attachmentName,
    });
    await traceRow.click();

    const traceFrame = page.locator(".modal__content #pw-trace-iframe");
    await expect(traceFrame).toBeVisible();

    const viewerRoot = page.frameLocator(".modal__content #pw-trace-iframe").locator("html");
    await expect(viewerRoot).toHaveAttribute("data-trace-loaded", "1");
  });

  test("does not apply the generic preview size limit to trace attachments", async ({ page }) => {
    await mockTraceViewer(page);

    await page.route(/\/data\/test-cases\/.*\.json(?:\?.*)?$/, async (route) => {
      const response = await route.fetch();
      const payload = await response.json();

      if (payload.name === playwrightTrace.caseName) {
        type TestStageNode = {
          attachments?: { name?: string; size?: number }[];
          steps?: TestStageNode[];
        };

        const updateTraceAttachment = (node?: TestStageNode) => {
          if (!node) {
            return;
          }

          node.attachments?.forEach((attachment) => {
            if (attachment.name === playwrightTrace.attachmentName) {
              attachment.size = ATTACHMENT_PREVIEW_MAX_BYTES + 1;
            }
          });
          node.steps?.forEach(updateTraceAttachment);
        };

        updateTraceAttachment(payload.testStage);
      }

      await route.fulfill({
        response,
        body: JSON.stringify(payload),
        contentType: "application/json",
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
    await expect(page).toHaveURL(/attachment=/);
    await expect(
      previewContainerFor(traceRow).locator(".attachment-preview__preview-message"),
    ).toHaveCount(0);

    const traceFrame = page.locator(".modal__content #pw-trace-iframe");
    const downloadLink = page.locator(".modal__title .attachment-preview__trace-download");

    await expect(traceFrame).toBeVisible();
    await expect(downloadLink).toBeVisible();
    await expect(downloadLink).toHaveAttribute("download", /\.zip$/);

    const viewerRoot = page.frameLocator(".modal__content #pw-trace-iframe").locator("html");
    await expect(viewerRoot).toHaveAttribute("data-trace-loaded", "1");
  });
});
