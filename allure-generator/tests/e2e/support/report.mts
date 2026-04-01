import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";
import { expect, type Page } from "playwright/test";
import { REPORT_MODES, type ReportMode } from "./fixtures.mts";

const moduleDirname = path.dirname(fileURLToPath(import.meta.url));
const generatorRoot = path.resolve(moduleDirname, "..", "..", "..");

export const reportRoot = path.join(generatorRoot, "build", "e2e");
export const directoryServerOrigin =
  process.env.PLAYWRIGHT_E2E_SERVER_ORIGIN || "http://127.0.0.1:4173";

export interface ReportLocationOptions {
  fixture: string;
  mode: ReportMode;
  route?: string;
}

interface OpenCaseFromTreeOptions extends ReportLocationOptions {
  tab: string;
  caseName: string;
  search?: string;
}

const normalizeRoute = (route = ""): string => {
  if (!route) {
    return "";
  }

  return route.startsWith("#") ? route.slice(1) : route;
};

export const getReportPath = ({ fixture, mode }: ReportLocationOptions): string =>
  path.join(reportRoot, fixture, mode, "index.html");

export const reportUrl = ({ fixture, mode, route = "" }: ReportLocationOptions): string => {
  const normalizedRoute = normalizeRoute(route);
  if (mode === REPORT_MODES.SINGLE_FILE) {
    const url = pathToFileURL(getReportPath({ fixture, mode }));
    url.hash = normalizedRoute;
    return url.toString();
  }

  const hash = normalizedRoute ? `#${normalizedRoute}` : "";
  return `${directoryServerOrigin}/${fixture}/${mode}/index.html${hash}`;
};

export const openReport = async (page: Page, options: ReportLocationOptions): Promise<void> => {
  await page.goto(reportUrl(options));
  await expect(page.locator(".side-nav__menu")).toBeVisible();
};

export const currentRoute = (page: Page): string => {
  const url = new URL(page.url());
  return normalizeRoute(url.hash);
};

export const openCaseFromTree = async (
  page: Page,
  { fixture, mode, tab, caseName, search = caseName }: OpenCaseFromTreeOptions,
): Promise<void> => {
  await openReport(page, { fixture, mode, route: tab });
  const searchInput = page.locator(".search__input");
  await expect(searchInput).toBeVisible();
  await searchInput.fill(search);
  const leaf = page.locator(".node__leaf", { hasText: caseName }).first();
  await expect(leaf).toBeVisible();
  await leaf.click();
};
