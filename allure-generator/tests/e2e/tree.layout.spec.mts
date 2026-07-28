import { expect, test } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { openCaseFromTree } from "./support/report.mts";

const fixture = fixtures.treeLongName;

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
});
