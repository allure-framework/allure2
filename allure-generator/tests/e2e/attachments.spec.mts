import { expect, test, type Locator, type Page } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { openCaseFromTree } from "./support/report.mts";
import { previewContainerFor, stepLocator } from "./support/ui.mts";

const allure2 = fixtures.allure2;

const expandStep = async (page: Page, title: string): Promise<Locator> => {
  const step = stepLocator(page, title);

  await step.locator(":scope > .step__title_hasContent").click();
  await expect(step).toHaveClass(/step_expanded/);
  return step;
};

test.describe("Generic Attachments", () => {
  test("renders code and table attachment viewers", async ({ page }) => {
    await openCaseFromTree(page, {
      fixture: allure2.name,
      mode: REPORT_MODES.DIRECTORY,
      tab: "suites",
      caseName: allure2.caseName,
    });

    const xmlRow = (await expandStep(page, allure2.steps.xml)).locator(".attachment-row", {
      hasText: allure2.attachments.xml,
    });
    await expect(xmlRow).toBeVisible();
    await xmlRow.click();
    await expect(previewContainerFor(xmlRow).locator(".attachment__code")).toBeVisible();

    const jsonRow = (await expandStep(page, allure2.steps.json)).locator(".attachment-row", {
      hasText: allure2.attachments.json,
    });
    await expect(jsonRow).toBeVisible();
    await jsonRow.click();
    await expect(previewContainerFor(jsonRow).locator(".attachment__code")).toContainText("{");

    const csvRow = (await expandStep(page, allure2.steps.csv)).locator(".attachment-row", {
      hasText: allure2.attachments.csv,
    });
    await expect(csvRow).toBeVisible();
    await csvRow.click();
    await expect(previewContainerFor(csvRow).locator(".attachment__table tr")).toHaveCount(3);

    const tsvRow = (await expandStep(page, allure2.steps.tsv)).locator(".attachment-row", {
      hasText: allure2.attachments.tsv,
    });
    await expect(tsvRow).toBeVisible();
    await tsvRow.click();
    await expect(previewContainerFor(tsvRow).locator(".attachment__table tr")).toHaveCount(2);
  });

  test("renders media and URI-list attachments", async ({ page }) => {
    await openCaseFromTree(page, {
      fixture: allure2.name,
      mode: REPORT_MODES.DIRECTORY,
      tab: "suites",
      caseName: allure2.caseName,
    });

    const pngRow = (await expandStep(page, allure2.steps.png)).locator(".attachment-row", {
      hasText: allure2.attachments.png,
    });
    await expect(pngRow).toBeVisible();
    await pngRow.click();
    await expect(previewContainerFor(pngRow).locator(".attachment__media")).toBeVisible();

    const svgRow = (await expandStep(page, allure2.steps.svg)).locator(".attachment-row", {
      hasText: allure2.attachments.svg,
    });
    await expect(svgRow).toBeVisible();
    await svgRow.click();
    await expect(previewContainerFor(svgRow).locator(".attachment__embed")).toBeVisible();

    const videoRow = (await expandStep(page, allure2.steps.video)).locator(".attachment-row", {
      hasText: allure2.attachments.video,
    });
    await expect(videoRow).toBeVisible();
    await videoRow.click();
    await expect(previewContainerFor(videoRow).locator("video")).toBeVisible();

    const uriRow = (await expandStep(page, allure2.steps.uri)).locator(".attachment-row", {
      hasText: allure2.attachments.uri,
    });
    await expect(uriRow).toBeVisible();
    await uriRow.click();
    await expect(previewContainerFor(uriRow).locator(".attachment__url .link")).toHaveCount(2);
    await expect(
      previewContainerFor(uriRow).locator(".attachment__url .link").first(),
    ).toHaveAttribute("href", /data\/attachments\//);
  });
});
