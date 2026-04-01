import { expect, test } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { currentRoute, openReport } from "./support/report.mts";
import { groupLocator, sortTreeBy, toggleMarkFilter, toggleStatusFilter } from "./support/ui.mts";

const newDemo = fixtures.newDemo;
const reportModes = [REPORT_MODES.SINGLE_FILE, REPORT_MODES.DIRECTORY] as const;

for (const mode of reportModes) {
  test.describe(`Tree Shared (${mode})`, () => {
    test("suites supports search, marks, filters, sorting, and keyboard navigation", async ({
      page,
    }) => {
      await openReport(page, { fixture: newDemo.name, mode, route: "suites" });
      await expect(page.locator(".pane__title-text")).toHaveText("Suites");

      const searchInput = page.locator(".search__input");
      await searchInput.fill("tag:smoke");
      await expect(
        page.locator(".node__leaf", { hasText: newDemo.cases.failedPullRequest }),
      ).toBeVisible();

      await searchInput.fill("tag:sm");
      await expect(page.locator(".tree__empty")).toHaveText("There are no items");

      await searchInput.fill(newDemo.cases.failedPullRequest);
      await expect(
        page.locator(".node__leaf", { hasText: newDemo.cases.failedPullRequest }),
      ).toBeVisible();
      await toggleStatusFilter(page, "failed");
      await expect(page.locator(".tree__empty")).toHaveText("There are no items");
      await toggleStatusFilter(page, "failed");
      await expect(
        page.locator(".node__leaf", { hasText: newDemo.cases.failedPullRequest }),
      ).toBeVisible();

      await toggleMarkFilter(page, "flaky");
      await expect(
        page.locator(".node__leaf", { hasText: newDemo.cases.failedPullRequest }),
      ).toBeVisible();
      await toggleMarkFilter(page, "flaky");

      await searchInput.fill(newDemo.cases.flakyNewFailedIssue);
      await toggleMarkFilter(page, "newFailed");
      await expect(
        page.locator(".node__leaf", { hasText: newDemo.cases.flakyNewFailedIssue }),
      ).toBeVisible();
      await toggleMarkFilter(page, "newFailed");

      await searchInput.fill(newDemo.cases.retriesStatusChangeIssue);
      await toggleMarkFilter(page, "retriesStatusChange");
      await expect(
        page.locator(".node__leaf", { hasText: newDemo.cases.retriesStatusChangeIssue }),
      ).toBeVisible();
      await toggleMarkFilter(page, "retriesStatusChange");

      await searchInput.fill("");
      const pullRequestsGroup = groupLocator(page, "io.qameta.allure.PullRequestsWebTest");
      await pullRequestsGroup.locator(":scope > .node__title").click();

      await sortTreeBy(page, "sorter.duration", "desc");
      await expect(
        pullRequestsGroup.locator(":scope > .node__children > .node__leaf .node__name").first(),
      ).toHaveText(newDemo.cases.passedPullRequest);

      await sortTreeBy(page, "sorter.status", "desc");
      await expect(
        pullRequestsGroup.locator(":scope > .node__children > .node__leaf .node__name").first(),
      ).toHaveText(newDemo.cases.failedPullRequest);

      await sortTreeBy(page, "sorter.name", "desc");
      await expect(
        pullRequestsGroup.locator(":scope > .node__children > .node__leaf .node__name").first(),
      ).toHaveText(newDemo.cases.passedPullRequest);

      await searchInput.fill("authorized user");
      const selectedLeaf = page
        .locator(".node__leaf", { hasText: newDemo.cases.passedPullRequest })
        .first();
      await expect(selectedLeaf).toBeVisible();
      await selectedLeaf.click();
      const firstSelectedRoute = currentRoute(page);
      await expect(page.locator(".test-result__name")).toContainText(
        newDemo.cases.passedPullRequest,
      );

      await page.locator(".pane__title-text").click();
      await page.keyboard.press("ArrowDown");
      await expect.poll(() => currentRoute(page)).not.toBe(firstSelectedRoute);
      await expect(page.locator(".test-result__name")).not.toContainText(
        newDemo.cases.passedPullRequest,
      );

      await page.keyboard.press("Escape");
      await expect.poll(() => currentRoute(page)).toMatch(/^suites\/[^/]+\/?$/);

      await page.keyboard.press("ArrowLeft");
      await expect.poll(() => currentRoute(page)).toBe("suites");
    });

    test("behaviors, categories, and packages expose their tree-specific affordances", async ({
      page,
    }) => {
      await openReport(page, { fixture: newDemo.name, mode, route: "behaviors" });
      await expect(page.locator(".pane__title-text")).toHaveText("Behaviors");
      await expect(groupLocator(page, newDemo.behaviors.feature)).toBeVisible();
      await groupLocator(page, newDemo.behaviors.feature).locator(":scope > .node__title").click();
      await expect(groupLocator(page, newDemo.behaviors.story)).toBeVisible();
      await expect(page.locator(".tree__download")).toHaveAttribute(
        "data-download",
        "data/behaviors.csv",
      );
      await page.locator(".tree__info").click();
      await expect(page.locator(".node__info.node__expanded").first()).toBeVisible();

      await openReport(page, { fixture: newDemo.name, mode, route: "categories" });
      await expect(page.locator(".pane__title-text")).toHaveText("Categories");
      await expect(groupLocator(page, newDemo.categories.group)).toBeVisible();
      await groupLocator(page, newDemo.categories.group).locator(":scope > .node__title").click();
      await expect(
        page.locator(".node__name", { hasText: newDemo.categories.statusMessage }),
      ).toBeVisible();
      await expect(page.locator(".tree__download")).toHaveAttribute(
        "data-download",
        "data/categories.csv",
      );

      await openReport(page, { fixture: newDemo.name, mode, route: "packages" });
      await expect(page.locator(".pane__title-text")).toHaveText("Packages");
      await expect(groupLocator(page, newDemo.packages.root)).toBeVisible();
      await groupLocator(page, newDemo.packages.root).locator(":scope > .node__title").click();
      await expect(groupLocator(page, newDemo.packages.className)).toBeVisible();
      await expect(page.locator(".tree__download")).toHaveCount(0);
    });
  });
}
