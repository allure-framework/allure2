import { expect, test } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { currentRoute, openReport } from "./support/report.mts";
import { createDeferredGate } from "./support/async.mts";

const uiDemo = fixtures.uiDemo;

test.describe("Timeline", () => {
  test("filters visible results by duration and links bars to test results", async ({ page }) => {
    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
      route: "timeline",
    });

    const bars = page.locator(".timeline__plot a");
    await expect.poll(() => bars.count()).toBeGreaterThan(10);
    await expect(page.locator(".timeline__group_title").filter({ hasText: "charlie.local" })).toHaveCount(1);
    await expect(
      page.locator(".timeline__group_title").filter({ hasText: "ops-runner-eu-1" }),
    ).toHaveCount(1);
    const initialBars = await bars.count();

    const handle = page.locator(".timeline__slider_handle");
    const handleBox = await handle.boundingBox();
    if (!handleBox) {
      throw new Error("Timeline slider handle is not visible");
    }

    await page.mouse.move(handleBox.x + handleBox.width / 2, handleBox.y + handleBox.height / 2);
    await page.mouse.down();
    await page.mouse.move(
      handleBox.x + handleBox.width / 2 + 220,
      handleBox.y + handleBox.height / 2,
      {
        steps: 10,
      },
    );
    await page.mouse.up();

    await expect.poll(() => bars.count()).toBeLessThan(initialBars);

    const firstBar = bars.first();
    await expect(firstBar).toHaveAttribute("href", /^#testresult\/.+$/);
    await firstBar.evaluate((node) => {
      node.dispatchEvent(new MouseEvent("click", { bubbles: true }));
    });
    await expect.poll(() => currentRoute(page)).toMatch(/^testresult\/.+$/);
    await expect(page.locator(".test-result__name")).toBeVisible();
  });

  test("renders the timeline brush controls for adjusting the visible window", async ({
    page,
  }) => {
    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
      route: "timeline",
    });

    const selection = page.locator(".brush .selection");
    await expect(selection).toBeVisible();
    await expect(page.locator(".brush .handle--w")).toBeVisible();
    await expect(page.locator(".brush .handle--e")).toBeVisible();
  });

  test("shows a loader and error splash when timeline data fails to load", async ({ page }) => {
    const gate = createDeferredGate();

    await page.route(/\/data\/timeline\.json(?:\?.*)?$/, async (route) => {
      gate.markSeen();
      await gate.wait;
      await route.fulfill({
        body: "{}",
        contentType: "application/json",
        status: 404,
      });
    });

    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
      route: "timeline",
    });

    await gate.seen;
    await expect(page.locator(".app__content .loader__text")).toHaveText("Loading...");

    gate.release();

    await expect(page.locator(".app__content .error-splash__title")).toHaveText("404");
    await expect(page.locator(".app__content .error-splash__message")).toContainText("Not Found");
  });
});
