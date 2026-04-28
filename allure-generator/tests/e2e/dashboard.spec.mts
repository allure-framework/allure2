import { expect, test, type Locator, type Page } from "playwright/test";
import { createDeferredGate } from "./support/async.mts";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { openReport } from "./support/report.mts";
import { readWidgetColumns } from "./support/ui.mts";

const uiDemo = fixtures.uiDemo;
type ShellRouteReuseWindow = Window & typeof globalThis & { __shellRouteReuseMarker?: string };

const escapeRegExp = (value: string): string => value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");

const exactWidgetTitle = (page: Page, title: string): Locator =>
  page.getByRole("heading", {
    name: new RegExp(`^\\s*${escapeRegExp(title)}\\s*$`),
  });

test.describe("Dashboards", () => {
  test("single-file overview and graphs expose the stock widgets", async ({ page }) => {
    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.SINGLE_FILE,
    });

    await expect(page.locator(".summary-widget__stats .splash__title")).toHaveText("19");
    await expect(
      page.locator(".widget__title", { hasText: uiDemo.widgets.summary }),
    ).toBeVisible();
    await expect(page.locator(".widget__title", { hasText: uiDemo.widgets.trend })).toBeVisible();
    await expect(
      page.locator(".widget__title", { hasText: uiDemo.widgets.behaviors }),
    ).toBeVisible();
    await expect(page.locator(".widget__title", { hasText: uiDemo.widgets.suites })).toBeVisible();
    await expect(
      page.locator(".widget__title", { hasText: uiDemo.widgets.categories }),
    ).toBeVisible();
    await expect(
      page.locator(".widget__title", { hasText: uiDemo.widgets.environment }),
    ).toBeVisible();
    await expect(
      page.locator(".widget__title", { hasText: uiDemo.widgets.executors }),
    ).toBeVisible();
    await expect(page.locator(".history-trend__chart .chart__svg")).toBeVisible();

    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.SINGLE_FILE,
      route: "graph",
    });

    for (const title of uiDemo.graphs) {
      await expect(exactWidgetTitle(page, title)).toBeVisible();
    }
    await expect(page.locator(".chart__svg")).toHaveCount(7);
  });

  test("directory mode loads overview and graphs through the external loader path", async ({
    page,
  }) => {
    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
    });

    await expect(
      page.locator(".widget__title", { hasText: uiDemo.widgets.summary }),
    ).toBeVisible();
    await expect(page.locator(".widget__title", { hasText: uiDemo.widgets.trend })).toBeVisible();
    await expect(
      page.locator(".widget__title", { hasText: uiDemo.widgets.executors }),
    ).toBeVisible();

    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
      route: "graph",
    });

    await expect(exactWidgetTitle(page, "Status")).toBeVisible();
    await expect(exactWidgetTitle(page, "Duration")).toBeVisible();
    await expect(page.locator(".chart__svg")).toHaveCount(7);
  });

  test("directory mode keeps overview and graph widget ordering stable", async ({ page }) => {
    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
    });

    expect(await readWidgetColumns(page)).toEqual([
      [
        expect.stringContaining(uiDemo.widgets.summary),
        expect.stringContaining(uiDemo.widgets.suites),
        expect.stringContaining(uiDemo.widgets.environment),
        expect.stringContaining(uiDemo.widgets.behaviors),
      ],
      [
        expect.stringContaining(uiDemo.widgets.trend),
        expect.stringContaining(uiDemo.widgets.categories),
        expect.stringContaining(uiDemo.widgets.executors),
      ],
    ]);

    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
      route: "graph",
    });
    await expect(exactWidgetTitle(page, uiDemo.graphs[0])).toBeVisible();
    await expect(exactWidgetTitle(page, uiDemo.graphs[6])).toBeVisible();

    expect(await readWidgetColumns(page)).toEqual([
      [
        expect.stringContaining(uiDemo.graphs[0]),
        expect.stringContaining(uiDemo.graphs[2]),
        expect.stringContaining(uiDemo.graphs[5]),
        expect.stringContaining(uiDemo.graphs[3]),
      ],
      [
        expect.stringContaining(uiDemo.graphs[1]),
        expect.stringContaining(uiDemo.graphs[4]),
        expect.stringContaining(uiDemo.graphs[6]),
      ],
    ]);
  });

  test("directory mode persists sidebar collapse and language selection", async ({ page }) => {
    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
    });

    const sideNav = page.locator(".side-nav");
    await page.locator(".side-nav__collapse").click();
    await expect(sideNav).toHaveClass(/side-nav_collapsed/);

    await page.locator(".side-nav__language-small").click();
    await page.locator('.language-select__item[data-id="fr"]').click();
    await expect(page.locator(".side-nav__language-small")).toContainText("fr");

    await page.reload();
    await expect(page.locator(".side-nav__menu")).toBeVisible();
    await expect(sideNav).toHaveClass(/side-nav_collapsed/);
    await expect(page.locator(".side-nav__language-small")).toContainText("fr");
  });

  test("directory mode boots with persisted isv translations", async ({ page }) => {
    await page.addInitScript(() => {
      window.localStorage.setItem("ALLURE_REPORT_SETTINGS", JSON.stringify({ language: "isv" }));
    });

    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
    });

    await expect(page.locator('.side-nav__link[href="#graph"]')).toContainText("Diagramy");
    await expect(
      page.locator(".widget__title", { hasText: uiDemo.widgets.summary }),
    ).toBeVisible();
    await expect(page.locator(".summary-widget__stats .splash__title")).toHaveText("19");
  });

  test("directory mode reuses the shell across client-side route changes", async ({ page }) => {
    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
    });

    const sideNav = page.locator(".side-nav");
    await page.locator(".side-nav__collapse").click();
    await expect(sideNav).toHaveClass(/side-nav_collapsed/);

    await page.evaluate(() => {
      (window as ShellRouteReuseWindow).__shellRouteReuseMarker = "alive";
    });

    await page.locator('.side-nav__link[href="#graph"]').click();
    await expect(exactWidgetTitle(page, "Status")).toBeVisible();
    await expect(sideNav).toHaveClass(/side-nav_collapsed/);
    await expect
      .poll(() => page.evaluate(() => (window as ShellRouteReuseWindow).__shellRouteReuseMarker))
      .toBe("alive");

    await page.locator('.side-nav__link[href="#"]').click();
    await expect(
      page.locator(".widget__title", { hasText: uiDemo.widgets.summary }),
    ).toBeVisible();
    await expect(sideNav).toHaveClass(/side-nav_collapsed/);
    await expect
      .poll(() => page.evaluate(() => (window as ShellRouteReuseWindow).__shellRouteReuseMarker))
      .toBe("alive");
  });

  test("directory mode persists overview widget arrangement", async ({ page }) => {
    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
    });

    const initialColumns = await readWidgetColumns(page);
    expect(initialColumns[0]).not.toContain(uiDemo.widgets.executors);

    await page.evaluate(() => {
      const storageKey = "ALLURE_REPORT_SETTINGS_WIDGETS";
      const widgets = Array.from(document.querySelectorAll(".widgets-grid__col")).map((column) =>
        Array.from(column.querySelectorAll<HTMLElement>(".widget"))
          .map((widget) => widget.getAttribute("data-id"))
          .filter((widget): widget is string => widget !== null),
      );

      const sourceIndex = widgets.findIndex((column) => column.includes("executors"));
      if (sourceIndex === -1) {
        throw new Error("Executors widget not found");
      }

      widgets[sourceIndex] = widgets[sourceIndex].filter((widget) => widget !== "executors");
      widgets[0] = [...widgets[0], "executors"];

      window.localStorage.setItem(storageKey, JSON.stringify({ widgets }));
    });

    await page.reload();
    await expect(page.locator(".side-nav__menu")).toBeVisible();
    const persistedColumns = await readWidgetColumns(page);
    expect(persistedColumns[0]).toContain(uiDemo.widgets.executors);
  });

  test("directory mode keeps chart tooltips and resize behavior after graph route changes", async ({
    page,
  }) => {
    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
    });

    await page.locator('.side-nav__link[href="#graph"]').click();
    await expect(exactWidgetTitle(page, "Status")).toBeVisible();

    const legendRow = page.locator(".chart__legend-row").first();
    await legendRow.hover();
    await expect(page.locator(".tooltip")).toBeVisible();

    await page.setViewportSize({ width: 1280, height: 900 });
    await expect(page.locator(".chart__svg").first()).toBeVisible();

    await page.locator('.side-nav__link[href="#"]').click();
    await expect(
      page.locator(".widget__title", { hasText: uiDemo.widgets.summary }),
    ).toBeVisible();

    await page.locator('.side-nav__link[href="#graph"]').click();
    await expect(exactWidgetTitle(page, "Status")).toBeVisible();
    await legendRow.hover();
    await expect(page.locator(".tooltip")).toBeVisible();
  });

  test("keeps sibling widgets visible when one widget load fails", async ({ page }) => {
    const gate = createDeferredGate();

    await page.route(/\/widgets\/executors\.json(?:\?.*)?$/, async (route) => {
      gate.markSeen();
      await gate.wait;
      await route.fulfill({
        body: "{}",
        contentType: "application/json",
        status: 500,
      });
    });

    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
    });

    await gate.seen;
    await expect(page.locator('.widget[data-id="executors"] .loader__text')).toHaveText(
      "Loading...",
    );
    await expect(page.locator('.widget[data-id="summary"] .summary-widget__stats')).toBeVisible();

    gate.release();

    await expect(page.locator('.widget[data-id="executors"] .error-splash__title')).toHaveText(
      "500",
    );
    await expect(
      page.locator('.widget[data-id="executors"] .error-splash__message'),
    ).toContainText("Internal Server Error");
    await expect(page.locator('.widget[data-id="summary"] .summary-widget__stats')).toBeVisible();
  });
});
