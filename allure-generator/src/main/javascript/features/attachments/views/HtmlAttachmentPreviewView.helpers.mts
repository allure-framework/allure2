const MiB = 1024 * 1024;

export const HTML_PREVIEW_MAX_BYTES = 10 * MiB;
export const HTML_PREVIEW_SOURCE_MAX_BYTES = 2 * MiB;
export const HTML_PREVIEW_MIN_HEIGHT = 320;
export const HTML_PREVIEW_INITIAL_HEIGHT = 480;
export const HTML_PREVIEW_MAX_INLINE_HEIGHT = 1600;
export const HTML_PREVIEW_RESIZE_MESSAGE_TYPE = "allure:html-preview:resize";

const HTML_PREVIEW_MAX_MESSAGE_HEIGHT = 10000;

const HTML_PREVIEW_CSP = [
  "default-src 'none'",
  "base-uri 'none'",
  "form-action 'none'",
  "object-src 'none'",
  "frame-src 'none'",
  "child-src 'none'",
  "worker-src 'none'",
  "connect-src 'none'",
  "navigate-to 'none'",
  "img-src data: blob: https:",
  "media-src data: blob: https:",
  "font-src data: https:",
  "style-src 'unsafe-inline' https:",
  "script-src 'unsafe-inline' 'unsafe-eval' https:",
].join("; ");

const HTML_LIKE_PATTERN = /<\/?[a-z][\s\S]*>/i;

const formatMiB = (bytes: number) => `${Math.round(bytes / MiB)} MiB`;

export const getHtmlPreviewByteLength = (content: string) => new Blob([content]).size;

export const getHtmlPreviewOversizeReason = () =>
  `HTML preview is disabled because the attachment is larger than ${formatMiB(
    HTML_PREVIEW_MAX_BYTES,
  )}.`;

export const getHtmlPreviewInvalidReason = () =>
  "HTML preview is disabled because the attachment does not look like HTML.";

export const isHtmlPreviewOversized = (size: unknown) =>
  typeof size === "number" && Number.isFinite(size) && size > HTML_PREVIEW_MAX_BYTES;

export const isRenderableHtmlPreview = (content: string) =>
  HTML_LIKE_PATTERN.test(content.replace(/^\uFEFF/, "").trim());

const createHtmlPreviewResizeScript = (token: string) => `
(() => {
  const type = ${JSON.stringify(HTML_PREVIEW_RESIZE_MESSAGE_TYPE)};
  const token = ${JSON.stringify(token)};
  const minHeight = ${HTML_PREVIEW_MIN_HEIGHT};
  const maxHeight = ${HTML_PREVIEW_MAX_MESSAGE_HEIGHT};
  let scheduled = false;

  const clamp = (value) => Math.min(Math.max(value, minHeight), maxHeight);

  const measure = () => {
    const body = document.body;
    const root = document.documentElement;
    const heights = [
      body ? body.scrollHeight : 0,
      body ? body.offsetHeight : 0,
      root ? root.scrollHeight : 0,
      root ? root.offsetHeight : 0,
    ];

    return clamp(Math.ceil(Math.max(...heights)));
  };

  const send = () => {
    scheduled = false;
    parent.postMessage({ type, token, height: measure() }, "*");
  };

  const schedule = () => {
    if (!scheduled) {
      scheduled = true;
      requestAnimationFrame(send);
    }
  };

  window.addEventListener("load", schedule);
  document.addEventListener("DOMContentLoaded", schedule);

  if ("ResizeObserver" in window) {
    const observer = new ResizeObserver(schedule);
    observer.observe(document.documentElement);
    if (document.body) {
      observer.observe(document.body);
    }
  }

  setTimeout(schedule, 0);
  setTimeout(schedule, 100);
  setTimeout(schedule, 500);
})();
`;

const removePreviewControlElements = (doc: Document) => {
  doc.querySelectorAll("base").forEach((element) => element.remove());
  doc.querySelectorAll("meta[http-equiv]").forEach((element) => {
    const httpEquiv = element.getAttribute("http-equiv")?.trim().toLowerCase();
    if (httpEquiv === "content-security-policy" || httpEquiv === "refresh") {
      element.remove();
    }
  });
};

const prependPreviewMeta = (doc: Document) => {
  const csp = doc.createElement("meta");
  csp.setAttribute("http-equiv", "Content-Security-Policy");
  csp.setAttribute("content", HTML_PREVIEW_CSP);

  const referrer = doc.createElement("meta");
  referrer.setAttribute("name", "referrer");
  referrer.setAttribute("content", "no-referrer");

  const charset = doc.createElement("meta");
  charset.setAttribute("charset", "utf-8");

  doc.head.prepend(referrer);
  doc.head.prepend(csp);
  doc.head.prepend(charset);
};

const appendResizeScript = (doc: Document, token: string) => {
  const script = doc.createElement("script");
  script.textContent = createHtmlPreviewResizeScript(token);
  doc.body.appendChild(script);
};

export const createHtmlPreviewSrcDoc = (content: string, token: string) => {
  const doc = new DOMParser().parseFromString(content.replace(/^\uFEFF/, ""), "text/html");

  removePreviewControlElements(doc);
  prependPreviewMeta(doc);
  appendResizeScript(doc, token);

  return `<!doctype html>\n${doc.documentElement.outerHTML}`;
};
