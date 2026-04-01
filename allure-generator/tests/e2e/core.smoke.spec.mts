import { expect, test } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { currentRoute, openCaseFromTree, openReport } from "./support/report.mts";

const newDemo = fixtures.newDemo;
const reportModes = [REPORT_MODES.SINGLE_FILE, REPORT_MODES.DIRECTORY] as const;

for (const mode of reportModes) {
  test.describe(`Core Smoke (${mode})`, () => {
    test("loads stock navigation and resolves deep links", async ({ page }) => {
      await openReport(page, { fixture: newDemo.name, mode });

      await expect(page).toHaveTitle("Allure Report");
      await expect(page.locator(".side-nav__menu .side-nav__text")).toHaveText(
        newDemo.topLevelTabs,
      );

      await openReport(page, { fixture: newDemo.name, mode, route: "suites" });
      await expect(page.locator(".pane__title-text")).toHaveText("Suites");

      await openCaseFromTree(page, {
        fixture: newDemo.name,
        mode,
        tab: "suites",
        caseName: newDemo.cases.failedPullRequest,
      });
      await expect(page.locator(".test-result__name")).toContainText(
        newDemo.cases.failedPullRequest,
      );

      const resultRoute = currentRoute(page);
      await openReport(page, {
        fixture: newDemo.name,
        mode,
        route: resultRoute,
      });
      await expect(page.locator(".test-result__name")).toContainText(
        newDemo.cases.failedPullRequest,
      );

      await page.getByRole("link", { name: "History" }).click();
      await expect(page.locator(".tab_active")).toContainText("History");
      const historyRoute = currentRoute(page);

      await openReport(page, {
        fixture: newDemo.name,
        mode,
        route: historyRoute,
      });
      await expect(page.locator(".tab_active")).toContainText("History");
      await expect(page.locator(".test-result-history__success-rate")).toContainText(
        "Success rate",
      );
    });

    test("shows a clear error when a test-result tab is unknown", async ({ page }) => {
      await openCaseFromTree(page, {
        fixture: newDemo.name,
        mode,
        tab: "suites",
        caseName: newDemo.cases.failedPullRequest,
      });

      const brokenRoute = `${currentRoute(page).replace(/\/$/, "")}/not-a-tab`;
      await openReport(page, {
        fixture: newDemo.name,
        mode,
        route: brokenRoute,
      });

      await expect(page.locator(".error-splash__message")).toHaveText('Tab "not-a-tab" not found');
    });
  });
}
