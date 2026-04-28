import translate from "../../helpers/t.mts";

type ReportFetchOptions = RequestInit & {
  contentType?: string;
};

type ReportDataErrorOptions = {
  cause?: unknown;
  message: string;
  status?: number;
  url?: string;
};

type ReportDataSource = {
  resolveUrl: (url: string, contentType: string) => Promise<string>;
};

const DEFAULT_REPORT_ERROR_STATUS = 500;
const REPORT_UUID_META_NAME = "allure-report-uuid";
const defaultReportErrorMessage = () => translate("errors.loadingFailed");
let cachedReportUuid: string | null | undefined;

const normalizeStatus = (status: number | undefined, fallback = DEFAULT_REPORT_ERROR_STATUS) =>
  typeof status === "number" && Number.isFinite(status) && status > 0 ? status : fallback;

export class ReportDataError extends Error {
  declare cause?: unknown;

  status: number;

  url: string;

  constructor({
    cause,
    message,
    status = DEFAULT_REPORT_ERROR_STATUS,
    url = "",
  }: ReportDataErrorOptions) {
    super(message);
    this.name = "ReportDataError";
    this.cause = cause;
    this.status = normalizeStatus(status);
    this.url = url;
  }
}

const isReportDataError = (error: unknown): error is ReportDataError =>
  error instanceof ReportDataError ||
  (error instanceof Error &&
    "status" in error &&
    typeof (error as { status?: unknown }).status === "number" &&
    "url" in error &&
    typeof (error as { url?: unknown }).url === "string");

export const normalizeReportDataError = (
  error: unknown,
  fallback: Partial<Pick<ReportDataError, "message" | "status" | "url">> = {},
) => {
  if (error instanceof ReportDataError) {
    return error;
  }

  if (isReportDataError(error)) {
    return new ReportDataError({
      cause: error,
      message: error.message || fallback.message || defaultReportErrorMessage(),
      status: normalizeStatus(error.status, fallback.status),
      url: error.url || fallback.url || "",
    });
  }

  if (error instanceof Error) {
    return new ReportDataError({
      cause: error,
      message: error.message || fallback.message || defaultReportErrorMessage(),
      status: fallback.status,
      url: fallback.url,
    });
  }

  if (typeof error === "string") {
    return new ReportDataError({
      cause: error,
      message: error || fallback.message || defaultReportErrorMessage(),
      status: fallback.status,
      url: fallback.url,
    });
  }

  return new ReportDataError({
    cause: error,
    message: fallback.message || defaultReportErrorMessage(),
    status: fallback.status,
    url: fallback.url,
  });
};

const ensureReportDataReady = (): Promise<boolean> =>
  new Promise(function (resolve) {
    (function waitForReady() {
      if (window.reportDataReady !== false) {
        return resolve(true);
      }
      setTimeout(waitForReady, 30);
    })();
  });

const getReportUuid = (): string | null => {
  if (typeof cachedReportUuid !== "undefined") {
    return cachedReportUuid;
  }

  const meta = document.querySelector<HTMLMetaElement>(`meta[name="${REPORT_UUID_META_NAME}"]`);
  const content = meta?.content?.trim();
  cachedReportUuid = content || null;
  return cachedReportUuid;
};

const withReportCacheBust = (url: string): string => {
  if (url.startsWith("data:") || url.startsWith("blob:")) {
    return url;
  }

  const reportUuid = getReportUuid();
  if (!reportUuid) {
    return url;
  }

  const [baseUrl, hash = ""] = url.split("#");
  const [pathname, search = ""] = baseUrl.split("?");
  const params = new URLSearchParams(search);
  params.set("reportUuid", reportUuid);
  const nextSearch = params.toString();

  return `${pathname}${nextSearch ? `?${nextSearch}` : ""}${hash ? `#${hash}` : ""}`;
};

const loadEmbeddedReportData = async (name: string): Promise<string> => {
  await ensureReportDataReady();
  const reportData = window.reportData;

  if (reportData?.[name]) {
    return reportData[name];
  }

  throw new ReportDataError({
    message: translate("errors.notFound"),
    status: 404,
    url: name,
  });
};

const createEmbeddedReportSource = (): ReportDataSource => ({
  resolveUrl: async (url, contentType) => {
    const value = await loadEmbeddedReportData(url);
    return `data:${contentType};base64,${value}`;
  },
});

const createDirectoryReportSource = (): ReportDataSource => ({
  resolveUrl: async (url) => withReportCacheBust(url),
});

const getReportDataSource = (): ReportDataSource =>
  window.reportData ? createEmbeddedReportSource() : createDirectoryReportSource();

export const reportDataUrl = async (
  url: string,
  contentType = "application/octet-stream",
): Promise<string> => {
  return getReportDataSource().resolveUrl(url, contentType);
};

const assertOk = (response: Response, url = response.url): Response => {
  if (!response.ok) {
    throw new ReportDataError({
      message: response.statusText || `Request failed with status ${response.status}`,
      status: response.status,
      url,
    });
  }

  return response;
};

const fetchReportData = async (
  url: string,
  { contentType = "application/octet-stream", ...options }: ReportFetchOptions = {},
): Promise<Response> => {
  const fetchUrl =
    url.startsWith("data:") || url.startsWith("blob:")
      ? url
      : await reportDataUrl(url, contentType);

  try {
    return await fetch(fetchUrl, options).then((response) => assertOk(response, url));
  } catch (error: unknown) {
    throw normalizeReportDataError(error, {
      status: DEFAULT_REPORT_ERROR_STATUS,
      url,
    });
  }
};

export const fetchReportJson = async <TPayload,>(
  url: string,
  options: ReportFetchOptions = {},
): Promise<TPayload> => {
  const response = await fetchReportData(url, {
    ...options,
    contentType: options.contentType ?? "application/json",
  });

  return response.json() as Promise<TPayload>;
};

export const fetchReportText = async (
  url: string,
  options: ReportFetchOptions = {},
): Promise<string> => {
  const response = await fetchReportData(url, options);

  return response.text();
};

export const fetchReportBlob = async (
  url: string,
  options: ReportFetchOptions = {},
): Promise<Blob> => {
  const response = await fetchReportData(url, options);

  return response.blob();
};
