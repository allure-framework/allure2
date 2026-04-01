import type { Locator, Page } from "playwright/test";

const escapeRegExp = (value: string): string => value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");

export const groupLocator = (page: Page, groupName: string): Locator =>
  page
    .locator(".node__title > .node__name")
    .filter({
      hasText: new RegExp(`^\\s*${escapeRegExp(groupName)}\\s*$`),
    })
    .first()
    .locator("xpath=ancestor::*[contains(concat(' ', normalize-space(@class), ' '), ' node ')][1]");

export const stepLocator = (page: Page, stepName: string): Locator =>
  page
    .locator(".step__title_hasContent > .step__name")
    .filter({
      hasText: new RegExp(`^\\s*${escapeRegExp(stepName)}\\s*$`),
    })
    .first()
    .locator("xpath=ancestor::*[contains(concat(' ', normalize-space(@class), ' '), ' step ')][1]");

export const previewContainerFor = (row: Locator): Locator => row.locator("xpath=..");

export const readWidgetColumns = (page: Page): Promise<string[][]> =>
  page
    .locator(".widgets-grid__col")
    .evaluateAll((columns) =>
      columns.map((column) =>
        Array.from(column.querySelectorAll<HTMLElement>(".widget__title")).map(
          (title) => title.textContent?.replace(/\s+/g, " ").trim() ?? "",
        ),
      ),
    );

export const sortTreeBy = async (
  page: Page,
  sorterName: string,
  direction: "asc" | "desc" = "desc",
): Promise<void> => {
  const sorter = page.locator(`.sorter__item[data-name="${sorterName}"]`);
  const targetIconSelector = direction === "asc" ? ".fa-sort-desc" : ".fa-sort-asc";

  for (let index = 0; index < 3; index += 1) {
    const state = await sorter.evaluate((element, iconSelector) => {
      const name = element.querySelector(".sorter__name");
      const icon = element.querySelector(iconSelector);

      return {
        active: name?.classList.contains("sorter_enabled") ?? false,
        iconEnabled: icon?.classList.contains("sorter_enabled") ?? false,
      };
    }, targetIconSelector);

    if (state.active && state.iconEnabled) {
      return;
    }

    await sorter.click();
  }

  throw new Error(`Unable to switch ${sorterName} to ${direction}`);
};

export const toggleMarkFilter = (page: Page, mark: string): Promise<void> =>
  page.locator(`.marks-toggle [data-mark="${mark}"]`).click();

export const toggleStatusFilter = (page: Page, status: string): Promise<void> =>
  page.locator(`.status-toggle [data-status="${status}"]`).click();
