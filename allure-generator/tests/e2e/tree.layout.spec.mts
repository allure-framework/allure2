import { expect, test } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { openCaseFromTree } from "./support/report.mts";

const fixture = fixtures.treeLongName;
const parametersHiddenPaneWidth = 360;

test.describe("Tree layout", () => {
  test("ellipsizes long test names before parameter text", async ({ page }, testInfo) => {
    await openCaseFromTree(page, {
      fixture: fixture.name,
      mode: REPORT_MODES.DIRECTORY,
      tab: "suites",
      caseName: fixture.caseName,
    });

    const title = page.locator(".node__title_active");
    const name = title.locator(".node__name");
    const parameters = title.locator(".node__parameters");
    await expect(name).toHaveText(fixture.caseName);
    await expect(parameters).toContainText(fixture.parameters[0]);
    await expect(parameters).toContainText(fixture.parameters[1]);

    const layout = await title.evaluate((element) => {
      const nameElement = element.querySelector<HTMLElement>(".node__name");
      const parameterElement = element.querySelector<HTMLElement>(".node__parameters");
      if (!nameElement || !parameterElement) {
        throw new Error("Expected the selected tree row to contain a name and parameters");
      }

      const nameBox = nameElement.getBoundingClientRect();
      const parameterBox = parameterElement.getBoundingClientRect();
      const nameTextRange = document.createRange();
      nameTextRange.selectNodeContents(nameElement);
      const nameTextBox = nameTextRange.getBoundingClientRect();
      const nameStyle = getComputedStyle(nameElement);
      const visibleNameTextRight =
        nameStyle.overflowX === "visible"
          ? nameTextBox.right
          : Math.min(nameTextBox.right, nameBox.right);

      return {
        titleWidth: element.getBoundingClientRect().width,
        nameClientWidth: nameElement.clientWidth,
        nameScrollWidth: nameElement.scrollWidth,
        nameTextWidth: nameTextBox.width,
        nameRight: nameBox.right,
        parametersLeft: parameterBox.left,
        visibleNameTextRight,
        visibleOverlapPixels: Math.max(0, visibleNameTextRight - parameterBox.left),
        nameOverflowX: nameStyle.overflowX,
        nameTextOverflow: nameStyle.textOverflow,
        nameWhiteSpace: nameStyle.whiteSpace,
      };
    });

    await testInfo.attach("Tree row layout", {
      body: Buffer.from(JSON.stringify(layout, null, 2)),
      contentType: "application/json",
    });
    await testInfo.attach("Tree row screenshot", {
      body: await title.screenshot(),
      contentType: "image/png",
    });

    expect(
      layout.nameScrollWidth,
      "The fixture must force the test name to overflow its layout box",
    ).toBeGreaterThan(layout.nameClientWidth);
    expect(
      layout.visibleNameTextRight,
      `The test name paints ${layout.visibleOverlapPixels}px into the parameter area`,
    ).toBeLessThanOrEqual(layout.parametersLeft);
    await expect(name).toHaveCSS("overflow-x", "hidden");
    await expect(name).toHaveCSS("text-overflow", "ellipsis");
    await expect(name).toHaveCSS("white-space", "nowrap");
  });

  test("lets parameters use the remaining space beside a short name", async ({
    page,
  }, testInfo) => {
    await openCaseFromTree(page, {
      fixture: fixture.name,
      mode: REPORT_MODES.DIRECTORY,
      tab: "suites",
      caseName: fixture.shortCaseName,
    });

    const title = page.locator(".node__title_active");
    const name = title.locator(".node__name");
    const parameters = title.locator(".node__parameters");
    await expect(name).toHaveText(fixture.shortCaseName);
    await expect(parameters).toContainText(fixture.longParameter);

    const layout = await title.evaluate((element) => {
      const nameElement = element.querySelector<HTMLElement>(".node__name");
      const parameterElement = element.querySelector<HTMLElement>(".node__parameters");
      const statsElement = element.querySelector<HTMLElement>(".node__stats");
      if (!nameElement || !parameterElement || !statsElement) {
        throw new Error("Expected the selected tree row to contain name, parameters, and stats");
      }

      const parameterBox = parameterElement.getBoundingClientRect();
      const statsBox = statsElement.getBoundingClientRect();

      return {
        titleWidth: element.getBoundingClientRect().width,
        nameClientWidth: nameElement.clientWidth,
        nameScrollWidth: nameElement.scrollWidth,
        parametersClientWidth: parameterElement.clientWidth,
        parametersScrollWidth: parameterElement.scrollWidth,
        trailingGap: statsBox.left - parameterBox.right,
        parametersDisplay: getComputedStyle(parameterElement).display,
      };
    });

    await testInfo.attach("Short-name tree row layout", {
      body: Buffer.from(JSON.stringify(layout, null, 2)),
      contentType: "application/json",
    });
    await testInfo.attach("Short-name tree row screenshot", {
      body: await title.screenshot(),
      contentType: "image/png",
    });

    expect(
      layout.nameClientWidth,
      "A short test name should remain fully visible next to oversized parameters",
    ).toBeGreaterThanOrEqual(layout.nameScrollWidth);
    expect(
      layout.parametersScrollWidth,
      "The fixture must force the parameter text to overflow its allocated space",
    ).toBeGreaterThan(layout.parametersClientWidth);
    expect(
      layout.trailingGap,
      "The parameter area should use the row space available before the duration",
    ).toBeLessThanOrEqual(16);
    expect(layout.parametersDisplay).not.toBe("none");
  });

  test("hides parameters when the tree pane is narrower than 360px", async ({ page }, testInfo) => {
    await openCaseFromTree(page, {
      fixture: fixture.name,
      mode: REPORT_MODES.DIRECTORY,
      tab: "suites",
      caseName: fixture.shortCaseName,
    });

    const title = page.locator(".node__title_active");
    const name = title.locator(".node__name");
    const parameters = title.locator(".node__parameters");
    await expect(parameters).toBeVisible();

    await page.locator("allure-side-by-side").evaluate((element, targetWidth) => {
      const layout = element as HTMLElement & {
        splitter?: { setSizes: (sizes: number[]) => void };
      };
      if (!layout.splitter) {
        throw new Error("Expected the side-by-side splitter to be initialized");
      }

      const totalWidth = layout.getBoundingClientRect().width;
      const leftPercent = (targetWidth / totalWidth) * 100;
      layout.splitter.setSizes([leftPercent, 100 - leftPercent]);
    }, parametersHiddenPaneWidth - 20);

    const leftPane = page.locator(".side-by-side__left");
    await expect
      .poll(() => leftPane.evaluate((element) => element.getBoundingClientRect().width))
      .toBeLessThan(parametersHiddenPaneWidth);
    await expect(parameters).toBeHidden();
    await expect(name).toBeVisible();

    const layout = await title.evaluate((element) => {
      const leftPane = document.querySelector<HTMLElement>(".side-by-side__left");
      const nameElement = element.querySelector<HTMLElement>(".node__name");
      const parameterElement = element.querySelector<HTMLElement>(".node__parameters");
      if (!leftPane || !nameElement || !parameterElement) {
        throw new Error("Expected the tree pane and selected tree row to be rendered");
      }

      return {
        paneWidth: leftPane.getBoundingClientRect().width,
        nameClientWidth: nameElement.clientWidth,
        nameScrollWidth: nameElement.scrollWidth,
        parametersDisplay: getComputedStyle(parameterElement).display,
      };
    });

    await testInfo.attach("Narrow tree pane layout", {
      body: Buffer.from(JSON.stringify(layout, null, 2)),
      contentType: "application/json",
    });
    await testInfo.attach("Narrow tree pane screenshot", {
      body: await title.screenshot(),
      contentType: "image/png",
    });

    expect(layout.paneWidth).toBeLessThan(parametersHiddenPaneWidth);
    expect(layout.parametersDisplay).toBe("none");
    expect(layout.nameClientWidth).toBeGreaterThanOrEqual(layout.nameScrollWidth);
  });
});
