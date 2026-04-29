import { expect, test, type Locator, type Page } from "playwright/test";
import { createDeferredGate } from "./support/async.mts";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { openCaseFromTree } from "./support/report.mts";
import { previewContainerFor } from "./support/ui.mts";

const attachmentsFixture = fixtures.attachments;

const attachmentRow = (page: Page, name: string): Locator =>
  page.locator(".attachment-row", {
    hasText: name,
  });

const expectImageToDecode = async (locator: Locator) => {
  await expect(locator).toBeVisible();
  await expect
    .poll(async () =>
      locator.evaluate((node) => {
        const image = node as HTMLImageElement;
        return {
          complete: image.complete,
          naturalHeight: image.naturalHeight,
          naturalWidth: image.naturalWidth,
        };
      }),
    )
    .toEqual(
      expect.objectContaining({
        complete: true,
        naturalHeight: expect.any(Number),
        naturalWidth: expect.any(Number),
      }),
    );
  await expect
    .poll(async () =>
      locator.evaluate((node) => {
        const image = node as HTMLImageElement;
        return image.naturalWidth > 0 && image.naturalHeight > 0;
      }),
    )
    .toBe(true);
};

test.describe("Generic Attachments", () => {
  test("renders code and table attachment viewers", async ({ page }) => {
    await openCaseFromTree(page, {
      fixture: attachmentsFixture.name,
      mode: REPORT_MODES.DIRECTORY,
      tab: "suites",
      caseName: attachmentsFixture.caseName,
    });

    const xmlRow = attachmentRow(page, attachmentsFixture.attachments.xml);
    await expect(xmlRow).toBeVisible();
    await xmlRow.click();
    await expect(page).not.toHaveURL(/attachment=/);
    await expect(previewContainerFor(xmlRow).locator(".attachment__code")).toBeVisible();

    const jsonRow = attachmentRow(page, attachmentsFixture.attachments.json);
    await expect(jsonRow).toBeVisible();
    await jsonRow.click();
    await expect(previewContainerFor(jsonRow).locator(".attachment__code")).toContainText("{");

    const csvRow = attachmentRow(page, attachmentsFixture.attachments.csv);
    await expect(csvRow).toBeVisible();
    await csvRow.click();
    await expect(previewContainerFor(csvRow).locator(".attachment__table tr")).toHaveCount(4);

    const tsvRow = attachmentRow(page, attachmentsFixture.attachments.tsv);
    await expect(tsvRow).toBeVisible();
    await tsvRow.click();
    await expect(previewContainerFor(tsvRow).locator(".attachment__table tr")).toHaveCount(4);
  });

  test("renders media and URI-list attachments", async ({ page }) => {
    await openCaseFromTree(page, {
      fixture: attachmentsFixture.name,
      mode: REPORT_MODES.DIRECTORY,
      tab: "suites",
      caseName: attachmentsFixture.caseName,
    });

    const pngRow = attachmentRow(page, attachmentsFixture.attachments.png);
    await expect(pngRow).toBeVisible();
    await pngRow.click();
    await expectImageToDecode(previewContainerFor(pngRow).locator(".attachment__media"));

    const jpegRow = attachmentRow(page, attachmentsFixture.attachments.jpeg);
    await expect(jpegRow).toBeVisible();
    await jpegRow.click();
    await expectImageToDecode(previewContainerFor(jpegRow).locator(".attachment__media"));

    const svgRow = attachmentRow(page, attachmentsFixture.attachments.svg);
    await expect(svgRow).toBeVisible();
    await svgRow.click();
    await expectImageToDecode(previewContainerFor(svgRow).locator(".attachment__media"));

    const videoRow = attachmentRow(page, attachmentsFixture.attachments.video);
    await expect(videoRow).toBeVisible();
    await videoRow.click();
    await expect(previewContainerFor(videoRow).locator("video")).toBeVisible();

    const uriRow = attachmentRow(page, attachmentsFixture.attachments.uri);
    await expect(uriRow).toBeVisible();
    await uriRow.click();
    await expect(previewContainerFor(uriRow).locator(".attachment__url .link")).toHaveCount(2);
    await expect(
      previewContainerFor(uriRow).locator(".attachment__url .link").first(),
    ).toHaveAttribute("href", attachmentsFixture.uriLinks.graphs);
    await expect(
      previewContainerFor(uriRow).locator(".attachment__url .link").nth(1),
    ).toHaveAttribute("href", attachmentsFixture.uriLinks.docs);
  });

  test("renders the image diff attachment with the custom viewer", async ({ page }) => {
    await openCaseFromTree(page, {
      fixture: attachmentsFixture.name,
      mode: REPORT_MODES.DIRECTORY,
      tab: "suites",
      caseName: attachmentsFixture.caseName,
    });

    const imageDiffRow = attachmentRow(page, attachmentsFixture.attachments.imageDiff);
    await expect(imageDiffRow).toBeVisible();
    await expect(imageDiffRow).toHaveAttribute("data-type", "application/vnd.allure.image.diff");
    await imageDiffRow.click();

    const preview = previewContainerFor(imageDiffRow);
    await expect(preview.locator(".screen-diff__content")).toBeVisible();
    await expect(preview.locator('.screen-diff__switchers input[value="diff"]')).toBeChecked();
    await expectImageToDecode(preview.locator(".screen-diff__image"));

    await preview.locator('.screen-diff__switchers input[value="overlay"]').check();
    await expect(preview.locator(".screen-diff__overlay")).toBeVisible();
  });

  test("renders a download fallback for unsupported attachment types", async ({ page }) => {
    await openCaseFromTree(page, {
      fixture: attachmentsFixture.name,
      mode: REPORT_MODES.DIRECTORY,
      tab: "suites",
      caseName: attachmentsFixture.caseName,
    });

    const cssRow = attachmentRow(page, attachmentsFixture.attachments.css);
    await expect(cssRow).toBeVisible();
    await cssRow.click();

    const preview = previewContainerFor(cssRow);
    const downloadLink = preview.locator(".link[download]");
    await expect(downloadLink).toBeVisible();
    await expect(downloadLink).toHaveAttribute("download", attachmentsFixture.attachments.css);
    await expect(downloadLink).toHaveAttribute("href", /data\/attachments\//);
  });

  test("renders svg attachments even when the server returns a generic MIME type", async ({
    page,
  }) => {
    await page.route(/\/data\/attachments\/.*\.svg(?:\?.*)?$/, async (route) => {
      await route.fulfill({
        body: `<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32"><rect width="32" height="32" fill="#97cc64"/></svg>`,
        contentType: "text/plain",
        status: 200,
      });
    });

    await openCaseFromTree(page, {
      fixture: attachmentsFixture.name,
      mode: REPORT_MODES.DIRECTORY,
      tab: "suites",
      caseName: attachmentsFixture.caseName,
    });

    const svgRow = attachmentRow(page, attachmentsFixture.attachments.svg);
    await expect(svgRow).toBeVisible();
    await svgRow.click();
    await expectImageToDecode(previewContainerFor(svgRow).locator(".attachment__media"));
  });

  test("keeps attachment MIME labels wired on inline rows", async ({ page }) => {
    await openCaseFromTree(page, {
      fixture: attachmentsFixture.name,
      mode: REPORT_MODES.DIRECTORY,
      tab: "suites",
      caseName: attachmentsFixture.caseName,
    });

    const xmlRow = attachmentRow(page, attachmentsFixture.attachments.xml);
    await expect(xmlRow.getByRole("img", { name: "application/xml" })).toBeVisible();
  });

  test("keeps the tree layout when switching tabs after expanding an inline attachment", async ({
    page,
  }) => {
    await openCaseFromTree(page, {
      fixture: attachmentsFixture.name,
      mode: REPORT_MODES.DIRECTORY,
      tab: "suites",
      caseName: attachmentsFixture.caseName,
    });

    const xmlRow = attachmentRow(page, attachmentsFixture.attachments.xml);
    await expect(xmlRow).toBeVisible();
    await xmlRow.click();
    await expect(previewContainerFor(xmlRow).locator(".attachment__code")).toBeVisible();

    await page.locator('.side-nav__link[href="#categories"]').click();

    await expect(page).toHaveURL(/#categories$/);
    await expect(page.locator(".side-nav__link_active[href=\"#categories\"]")).toBeVisible();
    await expect(page.locator(".app__content .pane__title-text").first()).toHaveText("Categories");
    await expect(
      page.locator(".app__content .side-by-side__left allure-tree-view .tree__content"),
    ).toBeVisible();
    await expect(
      page.locator(".app__content .side-by-side__right .empty-view__message"),
    ).toContainText("No item selected");
  });

  test("shows a loader and error view when attachment content fails to load", async ({ page }) => {
    await openCaseFromTree(page, {
      fixture: attachmentsFixture.name,
      mode: REPORT_MODES.DIRECTORY,
      tab: "suites",
      caseName: attachmentsFixture.caseName,
    });

    const gate = createDeferredGate();

    await page.route(/\/data\/attachments\/.*\.xml(?:\?.*)?$/, async (route) => {
      gate.markSeen();
      await gate.wait;
      await route.fulfill({
        body: "",
        contentType: "application/xml",
        status: 404,
      });
    });

    const xmlRow = attachmentRow(page, attachmentsFixture.attachments.xml);
    await expect(xmlRow).toBeVisible();
    await xmlRow.click();
    await expect(xmlRow).toHaveClass(/attachment-row_selected/);

    await gate.seen;

    const preview = previewContainerFor(xmlRow);
    await expect(preview.locator(".loader__text")).toHaveText("Loading...");

    gate.release();

    await expect(preview.locator(".error-splash__title")).toHaveText("404");
    await expect(preview.locator(".error-splash__message")).toContainText("Not Found");
  });
});
