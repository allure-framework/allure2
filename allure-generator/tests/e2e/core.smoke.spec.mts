import { expect, test } from "playwright/test";
import { createDeferredGate } from "./support/async.mts";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { currentRoute, openCaseFromTree, openReport } from "./support/report.mts";

const uiDemo = fixtures.uiDemo;
const reportModes = [REPORT_MODES.SINGLE_FILE, REPORT_MODES.DIRECTORY] as const;

for (const mode of reportModes) {
  test.describe(`Core Smoke (${mode})`, () => {
    test("loads stock navigation and resolves deep links", async ({ page }) => {
      await openReport(page, { fixture: uiDemo.name, mode });

      await expect(page).toHaveTitle("Allure Report");
      await expect(page.locator(".side-nav__menu .side-nav__text")).toHaveText(
        uiDemo.topLevelTabs,
      );

      await openReport(page, { fixture: uiDemo.name, mode, route: "suites" });
      await expect(page.locator(".pane__title-text")).toHaveText("Suites");

      await openCaseFromTree(page, {
        fixture: uiDemo.name,
        mode,
        tab: "suites",
        caseName: uiDemo.cases.failedPullRequest,
      });
      await expect(page.locator(".test-result__name")).toContainText(
        uiDemo.cases.failedPullRequest,
      );

      const resultRoute = currentRoute(page);
      await openReport(page, {
        fixture: uiDemo.name,
        mode,
        route: resultRoute,
      });
      await expect(page.locator(".test-result__name")).toContainText(
        uiDemo.cases.failedPullRequest,
      );

      await page.getByRole("link", { name: "History" }).click();
      await expect(page.locator(".tab_active")).toContainText("History");
      const historyRoute = currentRoute(page);

      await openReport(page, {
        fixture: uiDemo.name,
        mode,
        route: historyRoute,
      });
      await expect(page.locator(".tab_active")).toContainText("History");
      await expect(page.locator(".test-result-history__success-rate")).toContainText(
        "Success rate",
      );

      await openCaseFromTree(page, {
        fixture: uiDemo.name,
        mode,
        tab: "suites",
        caseName: uiDemo.cases.failedPullRequest,
      });
      await page.getByRole("link", { name: "Retries" }).click();
      await expect(page.locator(".tab_active")).toContainText("Retries");
      const retriesRoute = currentRoute(page);

      await openReport(page, {
        fixture: uiDemo.name,
        mode,
        route: retriesRoute,
      });
      await expect(page.locator(".tab_active")).toContainText("Retries");
      await expect(page.locator(".pane__section .label_status_failed").first()).toBeVisible();
    });

    test("shows a clear error when a test-result tab is unknown", async ({ page }) => {
      await openCaseFromTree(page, {
        fixture: uiDemo.name,
        mode,
        tab: "suites",
        caseName: uiDemo.cases.failedPullRequest,
      });

      const brokenRoute = `${currentRoute(page).replace(/\/$/, "")}/not-a-tab`;
      await openReport(page, {
        fixture: uiDemo.name,
        mode,
        route: brokenRoute,
      });

      await expect(page.locator(".error-splash__message")).toHaveText('Tab "not-a-tab" not found');
    });

    test("shows a loader and inline error when the tree test-result pane fails to load", async ({
      page,
    }) => {
      test.skip(mode !== REPORT_MODES.DIRECTORY, "directory mode only");

      await openReport(page, {
        fixture: uiDemo.name,
        mode,
        route: "suites",
      });

      const searchInput = page.locator(".search__input");
      await expect(searchInput).toBeVisible();
      await searchInput.fill(uiDemo.cases.failedPullRequest);

      const leaf = page.locator(".node__leaf", { hasText: uiDemo.cases.failedPullRequest }).first();
      await expect(leaf).toBeVisible();

      const caseRoute = (await leaf.getAttribute("href"))?.replace(/^#/, "");
      if (!caseRoute) {
        throw new Error("Test result route was not found");
      }

      const gate = createDeferredGate();

      await page.route(/\/data\/test-cases\/.*\.json(?:\?.*)?$/, async (route) => {
        gate.markSeen();
        await gate.wait;
        await route.fulfill({
          body: "{}",
          contentType: "application/json",
          status: 404,
        });
      });

      await page.goto("about:blank");

      await openReport(page, {
        fixture: uiDemo.name,
        mode,
        route: caseRoute,
      });

      await gate.seen;
      await expect(page.locator(".side-by-side__right .loader__text")).toHaveText("Loading...");

      gate.release();

      await expect(page.locator(".side-by-side__right .error-splash__title")).toHaveText("404");
      await expect(page.locator(".side-by-side__right .error-splash__message")).toHaveText(
        "Not Found",
      );
    });
  });
}
