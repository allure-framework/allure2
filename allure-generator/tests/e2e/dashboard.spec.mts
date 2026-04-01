import { expect, test, type Locator, type Page } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { openReport } from "./support/report.mts";
import { readWidgetColumns } from "./support/ui.mts";

const newDemo = fixtures.newDemo;

const escapeRegExp = (value: string): string => value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");

const exactWidgetTitle = (page: Page, title: string): Locator =>
  page.getByRole("heading", {
    name: new RegExp(`^\\s*${escapeRegExp(title)}\\s*$`),
  });

test.describe("Dashboards", () => {
  test("single-file overview and graphs expose the stock widgets", async ({ page }) => {
    await openReport(page, {
      fixture: newDemo.name,
      mode: REPORT_MODES.SINGLE_FILE,
    });

    await expect(page.locator(".summary-widget__stats .splash__title")).toHaveText("9");
    await expect(
      page.locator(".widget__title", { hasText: newDemo.widgets.summary }),
    ).toBeVisible();
    await expect(page.locator(".widget__title", { hasText: newDemo.widgets.trend })).toBeVisible();
    await expect(
      page.locator(".widget__title", { hasText: newDemo.widgets.behaviors }),
    ).toBeVisible();
    await expect(page.locator(".widget__title", { hasText: newDemo.widgets.suites })).toBeVisible();
    await expect(
      page.locator(".widget__title", { hasText: newDemo.widgets.categories }),
    ).toBeVisible();
    await expect(
      page.locator(".widget__title", { hasText: newDemo.widgets.environment }),
    ).toBeVisible();
    await expect(
      page.locator(".widget__title", { hasText: newDemo.widgets.executors }),
    ).toBeVisible();
    await expect(page.locator(".history-trend__chart .chart__svg")).toBeVisible();

    await openReport(page, {
      fixture: newDemo.name,
      mode: REPORT_MODES.SINGLE_FILE,
      route: "graph",
    });

    for (const title of newDemo.graphs) {
      await expect(exactWidgetTitle(page, title)).toBeVisible();
    }
    await expect(page.locator(".chart__svg")).toHaveCount(7);
  });

  test("directory mode loads overview and graphs through the external loader path", async ({
    page,
  }) => {
    await openReport(page, {
      fixture: newDemo.name,
      mode: REPORT_MODES.DIRECTORY,
    });

    await expect(
      page.locator(".widget__title", { hasText: newDemo.widgets.summary }),
    ).toBeVisible();
    await expect(page.locator(".widget__title", { hasText: newDemo.widgets.trend })).toBeVisible();
    await expect(
      page.locator(".widget__title", { hasText: newDemo.widgets.executors }),
    ).toBeVisible();

    await openReport(page, {
      fixture: newDemo.name,
      mode: REPORT_MODES.DIRECTORY,
      route: "graph",
    });

    await expect(exactWidgetTitle(page, "Status")).toBeVisible();
    await expect(exactWidgetTitle(page, "Duration")).toBeVisible();
    await expect(page.locator(".chart__svg")).toHaveCount(7);
  });

  test("directory mode persists sidebar collapse and language selection", async ({ page }) => {
    await openReport(page, {
      fixture: newDemo.name,
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

  test("directory mode persists overview widget arrangement", async ({ page }) => {
    await openReport(page, {
      fixture: newDemo.name,
      mode: REPORT_MODES.DIRECTORY,
    });

    const initialColumns = await readWidgetColumns(page);
    expect(initialColumns[0]).not.toContain(newDemo.widgets.executors);

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
    expect(persistedColumns[0]).toContain(newDemo.widgets.executors);
  });
});
