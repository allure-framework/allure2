import { expect, test } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { currentRoute, openReport } from "./support/report.mts";

const newDemo = fixtures.newDemo;

test.describe("Timeline", () => {
  test("filters visible results by duration and links bars to test results", async ({ page }) => {
    await openReport(page, {
      fixture: newDemo.name,
      mode: REPORT_MODES.DIRECTORY,
      route: "timeline",
    });

    await expect(page.locator(".timeline__item").first()).toBeVisible();
    const initialBars = await page.locator(".timeline__plot a").count();

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

    await expect.poll(() => page.locator(".timeline__plot a").count()).toBeLessThan(initialBars);

    await page.locator(".timeline__plot a").first().click();
    await expect.poll(() => currentRoute(page)).toMatch(/^testresult\/.+$/);
    await expect(page.locator(".test-result__name")).toBeVisible();
  });

  test("updates the visible window when the timeline brush changes", async ({ page }) => {
    await openReport(page, {
      fixture: newDemo.name,
      mode: REPORT_MODES.DIRECTORY,
      route: "timeline",
    });

    const selection = page.locator(".brush .selection");
    await expect(selection).toBeVisible();
    const initialSelectionBox = await selection.boundingBox();
    if (!initialSelectionBox) {
      throw new Error("Timeline brush selection is not visible");
    }

    const eastHandle = page.locator(".brush .handle--e");
    const eastHandleBox = await eastHandle.boundingBox();
    if (!eastHandleBox) {
      throw new Error("Timeline brush east handle is not visible");
    }

    await page.mouse.move(
      eastHandleBox.x + eastHandleBox.width / 2,
      eastHandleBox.y + eastHandleBox.height / 2,
    );
    await page.mouse.down();
    await page.mouse.move(
      eastHandleBox.x + eastHandleBox.width / 2 - 120,
      eastHandleBox.y + eastHandleBox.height / 2,
      { steps: 10 },
    );
    await page.mouse.up();

    await expect
      .poll(async () => {
        const selectionBox = await selection.boundingBox();
        return selectionBox?.width ?? 0;
      })
      .toBeLessThan(initialSelectionBox.width);
  });
});
