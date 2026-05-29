import { expect, test } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { currentRoute, openReport } from "./support/report.mts";
import { createDeferredGate } from "./support/async.mts";

const uiDemo = fixtures.uiDemo;

type TimelineNode = {
  children?: TimelineNode[];
  retry?: boolean;
  time?: {
    duration?: number;
    start?: number;
    stop?: number;
  };
};

const makeFirstTimelineLeafZeroDuration = (node: TimelineNode): boolean => {
  if (node.children) {
    return node.children.some(makeFirstTimelineLeafZeroDuration);
  }

  if (node.retry || typeof node.time?.start !== "number" || typeof node.time.stop !== "number") {
    return false;
  }

  node.time.stop = node.time.start;
  node.time.duration = 0;
  return true;
};

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

  test("renders retry results as status-colored hatched bars", async ({ page }) => {
    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
      route: "timeline",
    });

    const retryLinks = page.locator(".timeline__plot a:has(.timeline__item_retry)");
    await expect.poll(() => retryLinks.count()).toBeGreaterThan(0);

    const retryBars = page.locator(".timeline__item_retry");
    const retryCount = await retryBars.count();
    for (let index = 0; index < retryCount; index += 1) {
      const retryBar = retryBars.nth(index);
      const className = (await retryBar.getAttribute("class")) || "";
      const status = className.match(
        /\btimeline__item_status_(failed|broken|passed|skipped|unknown)\b/u,
      )?.[1];
      expect(status, `retry bar ${index} keeps its status class`).toBeTruthy();

      const fill = await retryBar.evaluate((node) => getComputedStyle(node).fill);
      expect(fill).toContain(`timeline-retry-${status}`);
    }

    const regularFill = await page
      .locator(".timeline__item:not(.timeline__item_retry)")
      .first()
      .evaluate((node) => getComputedStyle(node).fill);
    expect(regularFill).not.toContain("timeline-retry-");

    const firstRetryLink = retryLinks.first();
    await expect(firstRetryLink).toHaveAttribute("href", /^#testresult\/.+$/);
    await firstRetryLink.evaluate((node) => {
      node.dispatchEvent(new MouseEvent("click", { bubbles: true }));
    });
    await expect.poll(() => currentRoute(page)).toMatch(/^testresult\/.+$/);
    await expect(page.locator(".test-result__name")).toBeVisible();
  });

  test("renders zero-duration results as status-colored dots", async ({ page }) => {
    await page.route(/\/data\/timeline\.json(?:\?.*)?$/, async (route) => {
      const response = await route.fetch();
      const timeline = (await response.json()) as TimelineNode;

      if (!makeFirstTimelineLeafZeroDuration(timeline)) {
        throw new Error("Expected the timeline fixture to contain at least one non-retry result");
      }

      await route.fulfill({ json: timeline, response });
    });

    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
      route: "timeline",
    });

    const zeroDots = page.locator("circle.timeline__item_zero");
    await expect(zeroDots).toHaveCount(1);
    await expect(page.locator("rect.timeline__item_zero")).toHaveCount(0);
    await expect(page.locator("rect.timeline__item:not(.timeline__item_zero)")).not.toHaveCount(0);

    const zeroDot = zeroDots.first();
    const className = (await zeroDot.getAttribute("class")) || "";
    const status = className.match(
      /\btimeline__item_status_(failed|broken|passed|skipped|unknown)\b/u,
    )?.[1];
    expect(status, "zero-duration dot keeps its status class").toBeTruthy();

    await expect(zeroDot).toHaveAttribute("r", "5");
    await expect
      .poll(async () => Number.isFinite(Number(await zeroDot.getAttribute("cx"))))
      .toBe(true);

    const zeroResultLink = page.locator(".timeline__plot a:has(circle.timeline__item_zero)").first();
    await expect(zeroResultLink).toHaveAttribute("href", /^#testresult\/.+$/);
    await expect
      .poll(async () =>
        zeroResultLink.evaluate((node) => {
          const siblings = Array.from(node.parentElement?.children ?? []);
          const dotIndex = siblings.indexOf(node);

          return siblings
            .slice(dotIndex + 1)
            .every((sibling) => !sibling.querySelector("rect.timeline__item"));
        }),
      )
      .toBe(true);
    await zeroResultLink.evaluate((node) => {
      node.dispatchEvent(new MouseEvent("click", { bubbles: true }));
    });
    await expect.poll(() => currentRoute(page)).toMatch(/^testresult\/.+$/);
    await expect(page.locator(".test-result__name")).toBeVisible();
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
