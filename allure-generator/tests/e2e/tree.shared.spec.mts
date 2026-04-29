import { expect, test } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { currentRoute, openCaseFromTree, openReport } from "./support/report.mts";
import { groupLocator, sortTreeBy, toggleMarkFilter, toggleStatusFilter } from "./support/ui.mts";

const uiDemo = fixtures.uiDemo;
const reportModes = [REPORT_MODES.SINGLE_FILE, REPORT_MODES.DIRECTORY] as const;

for (const mode of reportModes) {
  test.describe(`Tree Shared (${mode})`, () => {
    test("suites supports search, marks, filters, sorting, and keyboard navigation", async ({
      page,
    }) => {
      await openReport(page, { fixture: uiDemo.name, mode, route: "suites" });
      await expect(page.locator(".pane__title-text")).toHaveText("Suites");

      const searchInput = page.locator(".search__input");
      await searchInput.fill("tag:smoke");
      await expect(
        page.locator(".node__leaf", { hasText: uiDemo.cases.failedPullRequest }),
      ).toBeVisible();

      await searchInput.fill("tag:sm");
      await expect(page.locator(".tree__empty")).toHaveText("There are no items");

      await searchInput.fill(uiDemo.cases.failedPullRequest);
      await expect(
        page.locator(".node__leaf", { hasText: uiDemo.cases.failedPullRequest }),
      ).toBeVisible();
      await toggleStatusFilter(page, "failed");
      await expect(page.locator(".tree__empty")).toHaveText("There are no items");
      await toggleStatusFilter(page, "failed");
      await expect(
        page.locator(".node__leaf", { hasText: uiDemo.cases.failedPullRequest }),
      ).toBeVisible();

      await toggleMarkFilter(page, "flaky");
      await expect(
        page.locator(".node__leaf", { hasText: uiDemo.cases.failedPullRequest }),
      ).toBeVisible();
      await toggleMarkFilter(page, "flaky");

      await searchInput.fill(uiDemo.cases.flakyNewFailedIssue);
      await toggleMarkFilter(page, "newFailed");
      await expect(
        page.locator(".node__leaf", { hasText: uiDemo.cases.flakyNewFailedIssue }),
      ).toBeVisible();
      await toggleMarkFilter(page, "newFailed");

      await searchInput.fill(uiDemo.cases.retriesStatusChangeIssue);
      await toggleMarkFilter(page, "retriesStatusChange");
      await expect(
        page.locator(".node__leaf", { hasText: uiDemo.cases.retriesStatusChangeIssue }),
      ).toBeVisible();
      await toggleMarkFilter(page, "retriesStatusChange");

      await searchInput.fill("");
      const pullRequestsGroup = groupLocator(page, "io.qameta.allure.PullRequestsWebTest");
      await pullRequestsGroup.locator(":scope > .node__title").click();

      await sortTreeBy(page, "sorter.duration", "desc");
      await expect(
        pullRequestsGroup.locator(":scope > .node__children > .node__leaf .node__name").first(),
      ).toHaveText(uiDemo.cases.passedPullRequest);

      await sortTreeBy(page, "sorter.status", "desc");
      await expect(
        pullRequestsGroup.locator(":scope > .node__children > .node__leaf .node__name").first(),
      ).toHaveText(uiDemo.cases.failedPullRequest);

      await sortTreeBy(page, "sorter.name", "desc");
      await expect(
        pullRequestsGroup.locator(":scope > .node__children > .node__leaf .node__name").first(),
      ).toHaveText(uiDemo.cases.passedPullRequest);

      await searchInput.fill("authorized user");
      const selectedLeaf = page.locator(".node__leaf", { hasText: uiDemo.cases.passedPullRequest }).first();
      await expect(selectedLeaf).toBeVisible();
      await selectedLeaf.click();
      const firstSelectedRoute = currentRoute(page);
      await expect(page.locator(".test-result__name")).toContainText(
        uiDemo.cases.passedPullRequest,
      );

      await page.locator(".pane__title-text").click();
      await page.keyboard.press("ArrowDown");
      await expect.poll(() => currentRoute(page)).not.toBe(firstSelectedRoute);
      await expect(page.locator(".test-result__name")).not.toContainText(
        uiDemo.cases.passedPullRequest,
      );

      await page.keyboard.press("Escape");
      await expect.poll(() => currentRoute(page)).toMatch(/^suites\/[^/]+\/?$/);

      await page.keyboard.press("ArrowLeft");
      await expect.poll(() => currentRoute(page)).toBe("suites");
    });

    test("behaviors, categories, and packages expose their tree-specific affordances", async ({
      page,
    }) => {
      await openReport(page, { fixture: uiDemo.name, mode, route: "behaviors" });
      await expect(page.locator(".pane__title-text")).toHaveText("Behaviors");
      await page.locator(".tree__info").hover();
      await expect(page.locator(".tooltip.tooltip_position_bottom")).toBeVisible();
      await expect(groupLocator(page, uiDemo.behaviors.epic)).toBeVisible();
      await groupLocator(page, uiDemo.behaviors.epic).locator(":scope > .node__title").click();
      await expect(groupLocator(page, uiDemo.behaviors.feature)).toBeVisible();
      await groupLocator(page, uiDemo.behaviors.feature).locator(":scope > .node__title").click();
      await expect(groupLocator(page, uiDemo.behaviors.story)).toBeVisible();
      await expect(page.locator(".tree__download")).toHaveAttribute(
        "data-download",
        "data/behaviors.csv",
      );
      await page.locator(".tree__info").click();
      await expect(page.locator(".node__info:visible").first()).toBeVisible();

      await openReport(page, { fixture: uiDemo.name, mode, route: "categories" });
      await expect(page.locator(".pane__title-text")).toHaveText("Categories");
      await expect(groupLocator(page, uiDemo.categories.group)).toBeVisible();
      await groupLocator(page, uiDemo.categories.group).locator(":scope > .node__title").click();
      await expect(
        page.locator(".node__name", { hasText: uiDemo.categories.statusMessage }),
      ).toBeVisible();
      await expect(page.locator(".tree__download")).toHaveAttribute(
        "data-download",
        "data/categories.csv",
      );

      await openReport(page, { fixture: uiDemo.name, mode, route: "packages" });
      await expect(page.locator(".pane__title-text")).toHaveText("Packages");
      await expect(groupLocator(page, uiDemo.packages.root)).toBeVisible();
      await groupLocator(page, uiDemo.packages.root).locator(":scope > .node__title").click();
      await expect(groupLocator(page, uiDemo.packages.className)).toBeVisible();
      await expect(page.locator(".tree__download")).toHaveCount(0);
    });

    test("collapsed groups stay collapsed when selecting leaves in other groups", async ({
      page,
    }) => {
      await openReport(page, { fixture: uiDemo.name, mode, route: "suites" });

      const topLevelGroups = page.locator(".tree__content > .node");
      const firstGroup = topLevelGroups.nth(0);
      const secondGroup = topLevelGroups.nth(1);

      await firstGroup.locator(":scope > .node__title").click();
      await firstGroup
        .locator(":scope > .node__children > .node__leaf .node__title")
        .first()
        .click();
      await expect(firstGroup).toHaveClass(/node__expanded/);

      await firstGroup.locator(":scope > .node__title").click();
      await expect(firstGroup).not.toHaveClass(/node__expanded/);

      await secondGroup.locator(":scope > .node__title").click();
      await secondGroup
        .locator(":scope > .node__children > .node__leaf .node__title")
        .first()
        .click();

      await expect(secondGroup).toHaveClass(/node__expanded/);
      await expect(firstGroup).not.toHaveClass(/node__expanded/);
    });

    test("keyboard navigation continues from the collapsed selected group", async ({ page }) => {
      await openReport(page, { fixture: uiDemo.name, mode, route: "suites" });

      const topLevelGroups = page.locator(".tree__content > .node");
      const firstGroup = topLevelGroups.nth(0);
      const secondGroup = topLevelGroups.nth(1);
      const firstGroupUid = await firstGroup.getAttribute("data-node-uid");
      const secondGroupUid = await secondGroup.getAttribute("data-node-uid");

      await firstGroup.locator(":scope > .node__title").click();
      await firstGroup
        .locator(":scope > .node__children > .node__leaf .node__title")
        .first()
        .click();
      await expect(firstGroup).toHaveClass(/node__expanded/);

      await firstGroup.locator(":scope > .node__title").click();
      await expect(firstGroup).not.toHaveClass(/node__expanded/);
      await expect.poll(() => currentRoute(page)).toBe(`suites/${firstGroupUid}`);

      await page.locator(".pane__title-text").click();
      await page.keyboard.press("ArrowDown");
      await expect.poll(() => currentRoute(page)).toBe(`suites/${secondGroupUid}`);
      await expect(secondGroup.locator(":scope > .node__title")).toHaveClass(/node__title_active/);
    });

    test("keyboard navigation walks visible groups and leaves in tree order", async ({ page }) => {
      await openReport(page, { fixture: uiDemo.name, mode, route: "suites" });

      const topLevelGroups = page.locator(".tree__content > .node");
      const firstGroup = topLevelGroups.nth(0);
      const secondGroup = topLevelGroups.nth(1);
      const firstGroupUid = await firstGroup.getAttribute("data-node-uid");
      const secondGroupUid = await secondGroup.getAttribute("data-node-uid");

      await firstGroup.locator(":scope > .node__title").click();
      const firstLeaf = firstGroup.locator(":scope > .node__children > .node__leaf .node__title").first();
      const firstLeafUid = await firstLeaf.getAttribute("data-uid");
      await expect(firstLeaf).toBeVisible();

      await page.locator(".pane__title-text").click();
      await page.keyboard.press("ArrowDown");
      await expect.poll(() => currentRoute(page)).toBe(`suites/${firstGroupUid}`);
      await expect(firstGroup.locator(":scope > .node__title")).toHaveClass(/node__title_active/);

      await page.keyboard.press("ArrowDown");
      await expect.poll(() => currentRoute(page)).toBe(`suites/${firstGroupUid}/${firstLeafUid}/`);
      await expect(firstLeaf).toHaveClass(/node__title_active/);
      await expect(page.locator(".test-result__name")).toBeVisible();

      await firstGroup.locator(":scope > .node__title").click();
      await expect(firstGroup).not.toHaveClass(/node__expanded/);

      await page.keyboard.press("ArrowDown");
      await expect.poll(() => currentRoute(page)).toBe(`suites/${secondGroupUid}`);
      await expect(secondGroup.locator(":scope > .node__title")).toHaveClass(/node__title_active/);
    });

    test("keyboard navigation expands and collapses the selected group", async ({ page }) => {
      await openReport(page, { fixture: uiDemo.name, mode, route: "suites" });

      const firstGroup = page.locator(".tree__content > .node").first();
      const firstGroupUid = await firstGroup.getAttribute("data-node-uid");

      await page.locator(".pane__title-text").click();
      await page.keyboard.press("ArrowDown");
      await expect.poll(() => currentRoute(page)).toBe(`suites/${firstGroupUid}`);
      await expect(firstGroup).not.toHaveClass(/node__expanded/);

      await page.keyboard.press("ArrowRight");
      await expect(firstGroup).toHaveClass(/node__expanded/);
      const firstChild = firstGroup.locator(":scope > .node__children > .node > .node__title").first();
      const firstChildUid = await firstChild.getAttribute("data-uid");
      const firstChildParentUid = await firstChild.getAttribute("data-parentUid");
      await expect(firstChild).toBeVisible();

      await page.keyboard.press("ArrowRight");
      await expect
        .poll(() => currentRoute(page))
        .toBe(
          firstChildParentUid
            ? `suites/${firstChildParentUid}/${firstChildUid}/`
            : `suites/${firstChildUid}`,
        );
      await expect(firstChild).toHaveClass(/node__title_active/);

      await page.keyboard.press("ArrowLeft");
      await expect.poll(() => currentRoute(page)).toBe(`suites/${firstGroupUid}`);
      await expect(firstGroup).not.toHaveClass(/node__expanded/);

      await page.keyboard.press("ArrowLeft");
      await expect(firstGroup).not.toHaveClass(/node__expanded/);
      await expect.poll(() => currentRoute(page)).toBe("suites");
    });
  });
}

test.describe("Tree Persistence", () => {
  test("directory mode preserves tree sorting and filter settings after reload", async ({
    page,
  }) => {
    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
      route: "suites",
    });

    await sortTreeBy(page, "sorter.duration", "desc");
    await toggleStatusFilter(page, "failed");
    await toggleMarkFilter(page, "flaky");
    await page.locator(".tree__info").click();

    await openReport(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
      route: "suites",
    });

    await expect(
      page.locator(
        '.sorter__item[data-name="sorter.duration"] .sorter__icon.lineArrowsSortLineDesc.sorter_enabled',
      ),
    ).toBeVisible();
    await expect(page.locator('.status-toggle .n-label[data-status="failed"]')).toBeVisible();
    await expect(page.locator('.marks-toggle .y-label-mark[data-mark="flaky"]')).toBeVisible();

    await expect
      .poll(() =>
        page.evaluate(() => {
          const settings = JSON.parse(
            window.localStorage.getItem("ALLURE_REPORT_SETTINGS_SUITES") ?? "{}",
          );
          return {
            sorter: settings.treeSorting?.sorter,
            ascending: settings.treeSorting?.ascending,
            failed: settings.visibleStatuses?.failed,
            flaky: settings.visibleMarks?.flaky,
            showGroupInfo: settings.showGroupInfo,
          };
        }),
      )
      .toEqual({
        sorter: "sorter.duration",
        ascending: false,
        failed: false,
        flaky: true,
        showGroupInfo: true,
      });
  });

  test("directory mode persists split-pane sizes after resize", async ({ page }) => {
    await openCaseFromTree(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
      tab: "suites",
      caseName: uiDemo.cases.failedPullRequest,
    });

    const gutter = page.locator(".gutter.gutter-horizontal");
    const gutterBox = await gutter.boundingBox();
    if (!gutterBox) {
      throw new Error("Side-by-side gutter is not visible");
    }

    await page.mouse.move(gutterBox.x + gutterBox.width / 2, gutterBox.y + gutterBox.height / 2);
    await page.mouse.down();
    await page.mouse.move(
      gutterBox.x + gutterBox.width / 2 + 120,
      gutterBox.y + gutterBox.height / 2,
    );
    await page.mouse.up();

    await expect
      .poll(() =>
        page.evaluate(() => {
          const settings = JSON.parse(
            window.localStorage.getItem("ALLURE_REPORT_SETTINGS") ?? "{}",
          );
          return settings.sideBySidePosition ?? null;
        }),
      )
      .not.toEqual([50, 50]);

    const savedSizes = await page.evaluate(() => {
      const settings = JSON.parse(window.localStorage.getItem("ALLURE_REPORT_SETTINGS") ?? "{}");
      return settings.sideBySidePosition ?? null;
    });
    if (!savedSizes) {
      throw new Error("Side-by-side sizes were not stored");
    }

    await openCaseFromTree(page, {
      fixture: uiDemo.name,
      mode: REPORT_MODES.DIRECTORY,
      tab: "suites",
      caseName: uiDemo.cases.failedPullRequest,
    });

    await expect
      .poll(() =>
        page.evaluate(() => {
          const settings = JSON.parse(
            window.localStorage.getItem("ALLURE_REPORT_SETTINGS") ?? "{}",
          );
          return settings.sideBySidePosition ?? null;
        }),
      )
      .toEqual(savedSizes);

    const restoredSplit = await page.evaluate(() => {
      const left = document.querySelector(".side-by-side__left");
      const right = document.querySelector(".side-by-side__right");
      const leftWidth = left?.getBoundingClientRect().width ?? 0;
      const rightWidth = right?.getBoundingClientRect().width ?? 0;

      return {
        leftPercent:
          leftWidth + rightWidth > 0 ? Math.round((leftWidth / (leftWidth + rightWidth)) * 100) : 0,
      };
    });

    expect(Math.abs(restoredSplit.leftPercent - Math.round(savedSizes[0]))).toBeLessThanOrEqual(8);
  });
});
