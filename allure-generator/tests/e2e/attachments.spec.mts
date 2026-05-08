import { expect, test, type Locator, type Page } from "playwright/test";
import { createDeferredGate } from "./support/async.mts";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { openCaseFromTree } from "./support/report.mts";
import { previewContainerFor } from "./support/ui.mts";

const attachmentsFixture = fixtures.attachments;

const escapeRegExp = (value: string) => value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");

const attachmentRow = (page: Page, name: string): Locator =>
  page.locator(".attachment-row").filter({
    has: page.locator(".attachment-row__name", {
      hasText: new RegExp(`^${escapeRegExp(name)}$`),
    }),
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

  for (const mode of [REPORT_MODES.SINGLE_FILE, REPORT_MODES.DIRECTORY] as const) {
    test(`renders the HTTP Exchange attachment with the custom viewer (${mode})`, async ({
      page,
    }) => {
      await openCaseFromTree(page, {
        fixture: attachmentsFixture.name,
        mode,
        tab: "suites",
        caseName: attachmentsFixture.caseName,
      });

      const httpRow = attachmentRow(page, attachmentsFixture.attachments.http);
      await expect(httpRow).toBeVisible();
      await expect(httpRow).toHaveAttribute("data-type", "application/vnd.allure.http+json");
      await httpRow.click();

      const preview = previewContainerFor(httpRow);
      await expect(preview.locator(".http-attachment__summary")).toBeVisible();
      await expect(preview.locator(".http-attachment__method")).toHaveText("POST");
      await expect(preview.locator(".http-attachment__url")).toHaveText(
        "https://api.example.com/v1/orders/42?dryRun=true",
      );
      await expect(preview.locator(".http-attachment__status")).toHaveText("201 Created");
      await expect(preview.locator(".http-attachment__duration")).toHaveText("87 ms");

      const requestSection = preview.locator(".http-attachment__section", {
        hasText: "Request",
      });
      await expect(requestSection).toContainText("HTTP/1.1");
      await expect(
        requestSection.locator(".http-attachment__label", { hasText: /^Method$/ }),
      ).toHaveCount(0);
      await expect(
        requestSection.locator(".http-attachment__label", { hasText: /^URL$/ }),
      ).toHaveCount(0);
      await expect(requestSection).not.toContainText("Coverage target");
      await expect(requestSection).toContainText("Authorization");
      await expect(requestSection).not.toContainText("__ALLURE_REDACTED__");
      await expect(requestSection).toContainText("dryRun");
      await expect(requestSection).toContainText("Cookies");
      await expect(requestSection).toContainText("sid");
      await expect(requestSection).toContainText("theme");
      const requestMaskedValues = requestSection.locator("[data-http-masked-value]");
      await expect(requestMaskedValues).toHaveCount(3);
      await expect(requestMaskedValues.first()).toHaveText("*****");
      await requestMaskedValues.first().hover();
      await expect(page.locator(".tooltip.tooltip_position_bottom")).toHaveText("Value is masked");
      await expect(requestSection.locator(".http-attachment__body-content .attachment__code")).toContainText(
        '{"name":"demo","quantity":1}',
      );
      await expect(requestSection.locator(".http-attachment__body-content .attachment__code")).toHaveClass(
        /language-json/,
      );
      const requestDownload = requestSection.getByRole("link", { name: "Download request" });
      await expect(requestDownload).toHaveAttribute("download", "request-body.json");
      await expect(requestDownload).toHaveAttribute(
        "href",
        /^data:application\/json;charset=utf-8,/,
      );

      const responseSection = preview.locator(".http-attachment__section", {
        hasText: "Response",
      });
      await expect(responseSection).toContainText("201 Created");
      await expect(responseSection).toContainText("HTTP/1.1");
      await expect(responseSection).toContainText("set-cookie");
      await expect(responseSection).toContainText("SameSite=Lax");
      await expect(responseSection).toContainText("HttpOnly");
      await expect(responseSection).not.toContainText("__ALLURE_REDACTED__");
      await expect(responseSection.locator("[data-http-masked-value]")).toHaveCount(2);
      await expect(responseSection.locator(".http-attachment__body-content .attachment__code")).toContainText(
        "<script>window.__httpAttachmentXss = true</script>",
      );
      await expect(responseSection.locator(".http-attachment__body-content .attachment__code")).toHaveClass(
        /language-json/,
      );
      const responseDownload = responseSection.getByRole("link", { name: "Download response" });
      await expect(responseDownload).toHaveAttribute("download", "response-body.json");
      await expect
        .poll(() =>
          page.evaluate(() =>
            Boolean(
              (window as Window & { __httpAttachmentXss?: boolean }).__httpAttachmentXss,
            ),
          ),
        )
        .toBe(false);
      await expect(preview.getByRole("button", { name: "Copy as curl" })).toHaveCount(0);
      await expect(preview.locator("[data-body-mode]")).toHaveCount(0);
    });

    test(`renders rich HTTP Exchange response bodies with attachment viewers (${mode})`, async ({
      page,
    }) => {
      await openCaseFromTree(page, {
        fixture: attachmentsFixture.name,
        mode,
        tab: "suites",
        caseName: attachmentsFixture.caseName,
      });

      const httpImageRow = attachmentRow(page, attachmentsFixture.attachments.httpImage);
      await expect(httpImageRow).toBeVisible();
      await expect(httpImageRow).toHaveAttribute("data-type", "application/vnd.allure.http+json");
      await httpImageRow.click();

      const preview = previewContainerFor(httpImageRow);
      await expect(preview.locator(".http-attachment__summary")).toBeVisible();
      await expect(preview.locator(".http-attachment__method")).toHaveText("GET");
      await expect(preview.locator(".http-attachment__status")).toHaveText("200 OK");

      const responseSection = preview.locator(".http-attachment__section", {
        hasText: "Response",
      });
      await expect(responseSection).toContainText("image/png");
      await expect(responseSection).toContainText("base64");
      await expect(responseSection).toContainText("1649 bytes");

      const richImage = responseSection.locator(".http-attachment__body-content .attachment__media");
      await expectImageToDecode(richImage);
      await expect
        .poll(() =>
          richImage.evaluate((node) => {
            const image = node as HTMLImageElement;
            return {
              height: image.naturalHeight,
              width: image.naturalWidth,
            };
          }),
        )
        .toEqual({
          height: 180,
          width: 620,
        });
      await expect(richImage).toHaveAttribute("src", /^data:image\/png;base64,/);
      const responseDownload = responseSection.getByRole("link", { name: "Download response" });
      await expect(responseDownload).toHaveAttribute("download", "response-body.png");
      await expect(responseDownload).toHaveAttribute("href", /^data:image\/png;base64,/);
    });

    test(`shows no-view placeholder for unsupported HTTP Exchange bodies (${mode})`, async ({
      page,
    }) => {
      await openCaseFromTree(page, {
        fixture: attachmentsFixture.name,
        mode,
        tab: "suites",
        caseName: attachmentsFixture.caseName,
      });

      const httpBinaryRow = attachmentRow(page, attachmentsFixture.attachments.httpBinary);
      await expect(httpBinaryRow).toBeVisible();
      await expect(httpBinaryRow).toHaveAttribute("data-type", "application/vnd.allure.http+json");
      await httpBinaryRow.click();

      const preview = previewContainerFor(httpBinaryRow);
      await expect(preview.locator(".http-attachment__method")).toHaveText("GET");
      await expect(preview.locator(".http-attachment__status")).toHaveText("200 OK");

      const responseSection = preview.locator(".http-attachment__section", {
        hasText: "Response",
      });
      await expect(responseSection).toContainText("application/octet-stream");
      await expect(responseSection).toContainText("base64");
      await expect(responseSection).toContainText("6 bytes");
      await expect(responseSection.locator(".http-attachment__body-message")).toHaveText(
        "No inline view for application/octet-stream.",
      );
      const responseDownload = responseSection.getByRole("link", { name: "Download response" });
      await expect(responseDownload).toHaveAttribute("download", "response-body.bin");
      await expect(responseDownload).toHaveAttribute(
        "href",
        /^data:application\/octet-stream;base64,/,
      );
    });

    test(`renders form HTTP Exchange bodies (${mode})`, async ({ page }) => {
      await openCaseFromTree(page, {
        fixture: attachmentsFixture.name,
        mode,
        tab: "suites",
        caseName: attachmentsFixture.caseName,
      });

      const httpFormRow = attachmentRow(page, attachmentsFixture.attachments.httpForm);
      await expect(httpFormRow).toBeVisible();
      await expect(httpFormRow).toHaveAttribute("data-type", "application/vnd.allure.http+json");
      await httpFormRow.click();

      const preview = previewContainerFor(httpFormRow);
      await expect(preview.locator(".http-attachment__method")).toHaveText("POST");
      await expect(preview.locator(".http-attachment__url")).toHaveText(
        "https://api.example.com/login",
      );
      await expect(preview.locator(".http-attachment__status")).toHaveText("204 No Content");

      const requestSection = preview.locator(".http-attachment__section", {
        hasText: "Request",
      });
      await expect(requestSection).toContainText("application/x-www-form-urlencoded");
      await expect(requestSection).toContainText("Form");
      await expect(requestSection).toContainText("username");
      await expect(requestSection).toContainText("demo");
      await expect(requestSection).toContainText("password");
      await expect(requestSection).not.toContainText("__ALLURE_REDACTED__");
      await expect(requestSection.locator("[data-http-masked-value]")).toHaveText("*****");
      await expect(requestSection).toContainText("remember");
      await expect(requestSection).toContainText("true");
      await expect(requestSection.getByRole("link", { name: "Download request" })).toHaveAttribute(
        "download",
        "request-body.txt",
      );
    });

    test(`renders multipart HTTP Exchange bodies (${mode})`, async ({ page }) => {
      await openCaseFromTree(page, {
        fixture: attachmentsFixture.name,
        mode,
        tab: "suites",
        caseName: attachmentsFixture.caseName,
      });

      const httpMultipartRow = attachmentRow(page, attachmentsFixture.attachments.httpMultipart);
      await expect(httpMultipartRow).toBeVisible();
      await expect(httpMultipartRow).toHaveAttribute(
        "data-type",
        "application/vnd.allure.http+json",
      );
      await httpMultipartRow.click();

      const preview = previewContainerFor(httpMultipartRow);
      await expect(preview.locator(".http-attachment__method")).toHaveText("POST");
      await expect(preview.locator(".http-attachment__url")).toHaveText(
        "https://api.example.com/profile",
      );
      await expect(preview.locator(".http-attachment__status")).toHaveText("200 OK");

      const requestSection = preview.locator(".http-attachment__section", {
        hasText: "Request",
      });
      await expect(requestSection).toContainText("multipart/form-data");
      await expect(requestSection).toContainText("24588 bytes");
      await expect(requestSection).toContainText("Parts");
      await expect(requestSection).toContainText("metadata");
      await expect(requestSection).toContainText("application/json");
      await expect(requestSection).toContainText('"displayName":"Demo User"');
      await expect(requestSection).toContainText("avatar | avatar.png");
      await expect(requestSection).toContainText("image/png");
      await expect(requestSection).toContainText("base64");
      await expect(requestSection).toContainText("24512 bytes");
      await expect(requestSection).toContainText("truncated");

      const responseSection = preview.locator(".http-attachment__section", {
        hasText: "Response",
      });
      await expect(responseSection.locator(".http-attachment__body-content .attachment__code")).toContainText(
        '{"uploaded":true}',
      );
    });

    test(`renders streaming HTTP Exchange bodies (${mode})`, async ({ page }) => {
      await openCaseFromTree(page, {
        fixture: attachmentsFixture.name,
        mode,
        tab: "suites",
        caseName: attachmentsFixture.caseName,
      });

      const httpStreamRow = attachmentRow(page, attachmentsFixture.attachments.httpStream);
      await expect(httpStreamRow).toBeVisible();
      await expect(httpStreamRow).toHaveAttribute("data-type", "application/vnd.allure.http+json");
      await httpStreamRow.click();

      const preview = previewContainerFor(httpStreamRow);
      await expect(preview.locator(".http-attachment__method")).toHaveText("GET");
      await expect(preview.locator(".http-attachment__url")).toHaveText(
        "https://api.example.com/events",
      );
      await expect(preview.locator(".http-attachment__status")).toHaveText("200 OK");

      const responseSection = preview.locator(".http-attachment__section", {
        hasText: "Response",
      });
      await expect(responseSection).toContainText("text/event-stream");
      await expect(responseSection).toContainText("truncated");
      await expect(responseSection).toContainText("Stream");
      await expect(responseSection).toContainText("server-sent-events");
      await expect(responseSection).toContainText("complete");
      await expect(responseSection).toContainText("false");
      await expect(responseSection).toContainText("chunkCount");
      await expect(responseSection).toContainText("1");
      await expect(responseSection.locator(".http-attachment__body-content .attachment__text")).toContainText(
        "event: ready",
      );
    });
  }

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
