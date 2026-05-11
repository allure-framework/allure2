import "./HttpAttachmentView.scss";
import { fetchReportJson } from "../../../core/services/reportData.mts";
import { createReportLoadErrorView, mountAsyncView } from "../../../core/view/asyncMount.mts";
import { bindDelegatedEvents } from "../../../core/view/domEvents.mts";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import { attachMountable, destroyMountable } from "../../../core/view/mountables.mts";
import { createElement } from "../../../shared/dom.mts";
import { createIconElement } from "../../../shared/icon/index.mts";
import TooltipView from "../../../shared/ui/TooltipView.mts";
import highlight from "../../../utils/highlight.mts";
import {
  isJsonAttachmentContentType,
  normalizeAttachmentContentType,
} from "../model/builtinAttachmentType.mts";
import attachmentType from "../model/attachmentType.mts";
import { HTTP_EXCHANGE_REDACTED_VALUE } from "../model/httpAttachment.mts";
import { renderAttachmentView } from "./renderAttachmentView.mts";

type Mountable = import("../../../core/view/types.mts").Mountable;
type Attachment = import("../../../types/report.mts").Attachment;
type AttachmentTypeInfo = import("../model/attachmentType.mts").AttachmentTypeInfo;
type DomChild = import("../../../shared/dom.mts").DomChild;
type HttpExchangePayload = import("../model/httpAttachment.mts").HttpExchangePayload;

type HttpAttachmentOptions = {
  sourceUrl: string;
};

type HttpPair = {
  name: string;
  value: string;
  masked: boolean;
};

type HttpCookie = HttpPair & {
  domain: string;
  expires: string;
  httpOnly: boolean;
  maxAge?: number;
  path: string;
  sameSite: string;
  secure: boolean;
};

type HttpBody = {
  contentType: string;
  encoding: string;
  value: string;
  hasValue: boolean;
  size?: number;
  truncated: boolean;
  form: HttpPair[];
  parts: HttpBodyPart[];
  stream: HttpStream | null;
};

type HttpBodyPart = {
  name: string;
  fileName: string;
  headers: HttpPair[];
  contentType: string;
  encoding: string;
  value: string;
  hasValue: boolean;
  size?: number;
  truncated: boolean;
};

type HttpStream = {
  type: string;
  complete: string;
  chunkCount?: number;
};

type HttpRequest = {
  method: string;
  url: string;
  httpVersion: string;
  headers: HttpPair[];
  cookies: HttpCookie[];
  query: HttpPair[];
  body: HttpBody | null;
  trailers: HttpPair[];
};

type HttpInformationalResponse = {
  status: string;
  reason: string;
  headers: HttpPair[];
};

type HttpResponse = {
  status: string;
  reason: string;
  httpVersion: string;
  headers: HttpPair[];
  cookies: HttpCookie[];
  body: HttpBody | null;
  trailers: HttpPair[];
  informationalResponses: HttpInformationalResponse[];
};

type HttpError = {
  name: string;
  message: string;
  stack: string;
};

type HttpDuration = {
  durationMs: string;
};

type NormalizedHttpPayload = {
  request: HttpRequest | null;
  response: HttpResponse | null;
  error: HttpError | null;
  duration: HttpDuration | null;
};

const DEFAULT_BODY_CONTENT_TYPE = "text/plain";
const DEFAULT_BINARY_CONTENT_TYPE = "application/octet-stream";
const MASKED_VALUE_PLACEHOLDER = "*****";
const MASKED_VALUE_TOOLTIP = "Value is masked";

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);

const toOptionalString = (value: unknown): string =>
  value === null || typeof value === "undefined" ? "" : String(value);

const toFiniteNumber = (value: unknown): number | undefined => {
  if (typeof value === "number" && Number.isFinite(value)) {
    return value;
  }

  if (typeof value === "string" && value.trim()) {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : undefined;
  }

  return undefined;
};

const isHttpPair = (value: HttpPair | null): value is HttpPair => value !== null;

const isHttpCookie = (value: HttpCookie | null): value is HttpCookie => value !== null;

const isHttpBodyPart = (value: HttpBodyPart | null): value is HttpBodyPart => value !== null;

const maskRedactedText = (value: string) =>
  value.replaceAll(HTTP_EXCHANGE_REDACTED_VALUE, MASKED_VALUE_PLACEHOLDER);

const renderMaskedValue = () =>
  createElement("span", {
    attrs: {
      "aria-label": MASKED_VALUE_TOOLTIP,
      "data-http-masked-value": true,
      tabindex: 0,
    },
    className: "http-attachment__masked-value",
    text: MASKED_VALUE_PLACEHOLDER,
  });

const renderPairValue = ({ masked, value }: HttpPair): DomChild =>
  masked ? renderMaskedValue() : maskRedactedText(value);

const normalizePair = (value: unknown): HttpPair | null => {
  if (!isRecord(value)) {
    return null;
  }

  const name = toOptionalString(value.name).trim();
  const pairValue = toOptionalString(value.value);
  const masked = pairValue === HTTP_EXCHANGE_REDACTED_VALUE;

  if (!name && !pairValue) {
    return null;
  }

  return {
    name,
    value: pairValue,
    masked,
  };
};

const normalizePairs = (value: unknown): HttpPair[] =>
  Array.isArray(value) ? value.map(normalizePair).filter(isHttpPair) : [];

const normalizeCookie = (value: unknown): HttpCookie | null => {
  if (!isRecord(value)) {
    return null;
  }

  const pair = normalizePair(value);
  if (!pair) {
    return null;
  }

  return {
    ...pair,
    domain: toOptionalString(value.domain).trim(),
    expires: toOptionalString(value.expires).trim(),
    httpOnly: value.httpOnly === true,
    maxAge: toFiniteNumber(value.maxAge),
    path: toOptionalString(value.path).trim(),
    sameSite: toOptionalString(value.sameSite).trim(),
    secure: value.secure === true,
  };
};

const normalizeCookies = (value: unknown): HttpCookie[] =>
  Array.isArray(value) ? value.map(normalizeCookie).filter(isHttpCookie) : [];

const headerValue = (headers: HttpPair[], name: string) => {
  const normalizedName = name.toLowerCase();

  return headers.find((header) => header.name.toLowerCase() === normalizedName)?.value || "";
};

const normalizeStream = (value: unknown): HttpStream | null => {
  if (!isRecord(value)) {
    return null;
  }

  return {
    type: toOptionalString(value.type).trim(),
    complete: typeof value.complete === "boolean" ? String(value.complete) : "",
    chunkCount: toFiniteNumber(value.chunkCount),
  };
};

const normalizeBodyPart = (value: unknown): HttpBodyPart | null => {
  if (!isRecord(value)) {
    return null;
  }

  const headers = normalizePairs(value.headers);
  const hasValue = typeof value.value !== "undefined" && value.value !== null;
  const encoding = toOptionalString(value.encoding).trim().toLowerCase() || "utf8";

  return {
    name: toOptionalString(value.name).trim(),
    fileName: toOptionalString(value.fileName).trim(),
    headers,
    contentType: toOptionalString(value.contentType).trim() || headerValue(headers, "content-type"),
    encoding,
    value: hasValue ? String(value.value) : "",
    hasValue,
    size: toFiniteNumber(value.size),
    truncated: value.truncated === true,
  };
};

const normalizeBodyParts = (value: unknown): HttpBodyPart[] =>
  Array.isArray(value) ? value.map(normalizeBodyPart).filter(isHttpBodyPart) : [];

const normalizeBody = (value: unknown, fallbackContentType = ""): HttpBody | null => {
  if (!isRecord(value)) {
    return null;
  }

  const hasValue = typeof value.value !== "undefined" && value.value !== null;
  const encoding = toOptionalString(value.encoding).trim().toLowerCase() || "utf8";

  return {
    contentType: toOptionalString(value.contentType).trim() || fallbackContentType,
    encoding,
    value: hasValue ? String(value.value) : "",
    hasValue,
    size: toFiniteNumber(value.size),
    truncated: value.truncated === true,
    form: normalizePairs(value.form),
    parts: normalizeBodyParts(value.parts),
    stream: normalizeStream(value.stream),
  };
};

const normalizeRequest = (value: unknown): HttpRequest | null => {
  if (!isRecord(value)) {
    return null;
  }

  const method = toOptionalString(value.method).trim().toUpperCase() || "GET";
  const headers = normalizePairs(value.headers);

  return {
    method,
    url: toOptionalString(value.url).trim(),
    httpVersion: toOptionalString(value.httpVersion).trim(),
    headers,
    cookies: normalizeCookies(value.cookies),
    query: normalizePairs(value.query),
    body: normalizeBody(value.body, headerValue(headers, "content-type")),
    trailers: normalizePairs(value.trailers),
  };
};

const normalizeInformationalResponse = (value: unknown): HttpInformationalResponse | null => {
  if (!isRecord(value)) {
    return null;
  }

  return {
    status: toOptionalString(value.status).trim(),
    reason: toOptionalString(value.statusText).trim(),
    headers: normalizePairs(value.headers),
  };
};

const normalizeInformationalResponses = (value: unknown): HttpInformationalResponse[] =>
  Array.isArray(value)
    ? value
        .map(normalizeInformationalResponse)
        .filter((response): response is HttpInformationalResponse => response !== null)
    : [];

const normalizeResponse = (value: unknown): HttpResponse | null => {
  if (!isRecord(value)) {
    return null;
  }

  const headers = normalizePairs(value.headers);

  return {
    status: toOptionalString(value.status).trim(),
    reason: toOptionalString(value.statusText).trim(),
    httpVersion: toOptionalString(value.httpVersion).trim(),
    headers,
    cookies: normalizeCookies(value.cookies),
    body: normalizeBody(value.body, headerValue(headers, "content-type")),
    trailers: normalizePairs(value.trailers),
    informationalResponses: normalizeInformationalResponses(value.informationalResponses),
  };
};

const normalizeError = (value: unknown): HttpError | null => {
  if (!isRecord(value)) {
    return null;
  }

  const name = toOptionalString(value.name).trim();
  const message = toOptionalString(value.message).trim();
  const stack = toOptionalString(value.stack).trim();

  if (!name && !message && !stack) {
    return null;
  }

  return {
    name,
    message,
    stack,
  };
};

const normalizeDuration = (value: unknown): HttpDuration | null => {
  if (!isRecord(value)) {
    return null;
  }

  const start = toFiniteNumber(value.start);
  const stop = toFiniteNumber(value.stop);
  if (typeof start === "undefined" || typeof stop === "undefined" || stop < start) {
    return null;
  }

  return {
    durationMs: `${stop - start} ms`,
  };
};

const normalizePayload = (payload: HttpExchangePayload): NormalizedHttpPayload => ({
  request: normalizeRequest(payload.request),
  response: normalizeResponse(payload.response),
  error: normalizeError(payload.error),
  duration: normalizeDuration(payload),
});

const getRequestTarget = (request: HttpRequest) => request.url || "";

const normalizeContentType = (contentType: string) =>
  normalizeAttachmentContentType(contentType || DEFAULT_BODY_CONTENT_TYPE);

const isJsonContentType = (contentType: string) =>
  isJsonAttachmentContentType(normalizeContentType(contentType));

const isHtmlContentType = (contentType: string) => {
  const normalized = normalizeContentType(contentType);

  return normalized === "text/html" || normalized === "application/xhtml+xml";
};

const canRenderSourceBody = (type: string | null) =>
  type === "image" || type === "svg" || type === "video";

const canRenderTextBody = (type: string | null) =>
  type === "code" || type === "text" || type === "table" || type === "uri";

const bodyCodeLanguage = (contentType: string) => {
  const normalized = normalizeContentType(contentType);

  if (isJsonContentType(normalized)) {
    return "json";
  }

  return (
    normalized
      .split("/")
      .pop()
      ?.replace(/[^a-z0-9_-]/gi, "-") || ""
  );
};

const highlightBodyCode = (container: HTMLElement, body: HttpBody) => {
  const codeBlock = container.querySelector(".attachment__code") as HTMLElement | null;

  if (!codeBlock) {
    return container;
  }

  const language = bodyCodeLanguage(body.contentType);
  if (language) {
    codeBlock.classList.add(`language-${language}`);
  }
  highlight.highlightElement(codeBlock);

  return container;
};

const createBodyAttachment = (body: HttpBody): Attachment => ({
  name: body.contentType || "body",
  size: body.size,
  source: "",
  type: body.contentType || DEFAULT_BINARY_CONTENT_TYPE,
});

const getSafeBodyAttachmentType = (body: HttpBody): AttachmentTypeInfo => {
  const contentType = normalizeContentType(body.contentType);
  const bodyAttachmentType = attachmentType(contentType);

  if (bodyAttachmentType.type === "html" || isHtmlContentType(contentType)) {
    return {
      type: "code",
      icon: "lineDevCodeSquare",
      parser: (value) => value,
    };
  }

  return bodyAttachmentType;
};

const createBase64DataUrl = (body: HttpBody) =>
  `data:${body.contentType || DEFAULT_BINARY_CONTENT_TYPE};base64,${body.value.replace(/\s/g, "")}`;

const createUtf8DataUrl = (body: HttpBody) =>
  `data:${body.contentType || DEFAULT_BODY_CONTENT_TYPE};charset=utf-8,${encodeURIComponent(body.value)}`;

const bodyDownloadExtension = (body: HttpBody) => {
  const contentType = normalizeContentType(body.contentType);
  const attachmentInfo = getSafeBodyAttachmentType(body);

  if (isJsonContentType(contentType)) {
    return "json";
  }

  const extensionByContentType: Record<string, string> = {
    "application/xhtml+xml": "html",
    "image/jpeg": "jpg",
    "image/jpg": "jpg",
    "text/tab-separated-values": "tsv",
  };
  if (extensionByContentType[contentType]) {
    return extensionByContentType[contentType];
  }

  if (attachmentInfo.type) {
    const subtype = contentType
      .split("/")
      .pop()
      ?.split("+")
      .pop()
      ?.replace(/[^a-z0-9_-]/gi, "");

    if (subtype) {
      return subtype;
    }
  }

  return body.encoding === "base64" ? "bin" : "txt";
};

const createBodyDownloadUrl = (body: HttpBody) =>
  body.encoding === "base64" ? createBase64DataUrl(body) : createUtf8DataUrl(body);

const renderBodyDownload = (body: HttpBody | null, name: "request" | "response") =>
  body?.hasValue
    ? createElement("a", {
        attrs: {
          download: `${name}-body.${bodyDownloadExtension(body)}`,
          href: createBodyDownloadUrl(body),
        },
        className: "http-attachment__download",
        children: [
          createIconElement("lineGeneralDownloadCloud", {
            inline: true,
            size: "s",
          }),
          createElement("span", {
            text: `Download ${name}`,
          }),
        ],
      })
    : null;

const renderBodyFallbackCode = (value: string) =>
  renderAttachmentView({
    attachment: {
      name: "body",
      source: "",
      type: DEFAULT_BODY_CONTENT_TYPE,
    },
    content: value,
    fullScreen: false,
    type: "code",
  });

const renderNoBodyView = (body: HttpBody) =>
  createElement("div", {
    className: "http-attachment__body-message",
    text: `No inline view for ${body.contentType || "this content type"}.`,
  });

const renderBodyValue = (body: HttpBody) => {
  const attachmentInfo = getSafeBodyAttachmentType(body);
  const attachment = createBodyAttachment(body);

  if (body.encoding === "base64") {
    if (canRenderSourceBody(attachmentInfo.type)) {
      return renderAttachmentView({
        attachment,
        fullScreen: false,
        sourceUrl: createBase64DataUrl(body),
        type: attachmentInfo.type,
      });
    }

    return renderNoBodyView(body);
  }

  if (canRenderSourceBody(attachmentInfo.type)) {
    return renderAttachmentView({
      attachment,
      fullScreen: false,
      sourceUrl: createUtf8DataUrl(body),
      type: attachmentInfo.type,
    });
  }

  if (canRenderTextBody(attachmentInfo.type)) {
    const contentValue = maskRedactedText(body.value);
    const content = attachmentInfo.parser ? attachmentInfo.parser(contentValue) : contentValue;

    const view = renderAttachmentView({
      attachment,
      content,
      fullScreen: false,
      type: attachmentInfo.type,
    });

    return attachmentInfo.type === "code" ? highlightBodyCode(view, body) : view;
  }

  return renderNoBodyView(body);
};

const renderInlinePairs = (pairs: HttpPair[]): DomChild[] =>
  pairs.flatMap((pair, index) => [
    index > 0 ? "; " : null,
    createElement("span", {
      children: [`${pair.name}: `, renderPairValue(pair)],
    }),
  ]);

const renderBodyPartSummary = (part: HttpBodyPart) =>
  [
    part.contentType || null,
    part.encoding && part.encoding !== "utf8" ? part.encoding : null,
    typeof part.size === "number" ? `${part.size} bytes` : null,
    part.truncated ? "truncated" : null,
  ]
    .filter((value): value is string => Boolean(value))
    .join(" | ");

const renderBodyPartsTable = (parts: HttpBodyPart[]) =>
  parts.length
    ? createElement("div", {
        className: "http-attachment__subsection",
        children: [
          createElement("h4", {
            className: "http-attachment__subsection-title",
            text: "Parts",
          }),
          createElement("table", {
            className: "http-attachment__table",
            children: createElement("tbody", {
              children: parts.map((part) =>
                createElement("tr", {
                  children: [
                    createElement("td", {
                      className: "http-attachment__pair-name",
                      text: [part.name, part.fileName].filter(Boolean).join(" | "),
                    }),
                    createElement("td", {
                      className: "http-attachment__pair-value",
                      children: [
                        createElement("div", {
                          text: renderBodyPartSummary(part),
                        }),
                        part.headers.length
                          ? createElement("div", {
                              children: renderInlinePairs(part.headers),
                            })
                          : null,
                        part.hasValue
                          ? createElement("pre", {
                              className: "http-attachment__part-value",
                              children:
                                part.value === HTTP_EXCHANGE_REDACTED_VALUE
                                  ? renderMaskedValue()
                                  : maskRedactedText(part.value),
                            })
                          : null,
                      ],
                    }),
                  ],
                }),
              ),
            }),
          }),
        ],
      })
    : null;

const renderStreamTable = (stream: HttpStream | null) => {
  if (!stream) {
    return null;
  }

  return renderPairsTable(
    "Stream",
    [
      stream.type
        ? {
            name: "type",
            value: stream.type,
            masked: false,
          }
        : null,
      stream.complete
        ? {
            name: "complete",
            value: stream.complete,
            masked: false,
          }
        : null,
      typeof stream.chunkCount === "number"
        ? {
            name: "chunkCount",
            value: String(stream.chunkCount),
            masked: false,
          }
        : null,
    ].filter(isHttpPair),
  );
};

const renderStructuredBodyContent = (body: HttpBody) => {
  if (body.form.length) {
    return renderPairsTable("Form", body.form);
  }

  if (body.parts.length) {
    return renderBodyPartsTable(body.parts);
  }

  if (body.stream) {
    return createElement("div", {
      className: "http-attachment__structured-body",
      children: [renderStreamTable(body.stream), body.hasValue ? renderBodyValue(body) : null],
    });
  }

  return null;
};

const renderBodyContent = (body: HttpBody) =>
  createElement("div", {
    className: "http-attachment__body-content",
    children: renderStructuredBodyContent(body) || renderBodyValue(body),
  });

const renderMetaRow = (name: string, value: string | null | undefined) =>
  value
    ? createElement("div", {
        className: "http-attachment__meta-row",
        children: [
          createElement("div", {
            className: "http-attachment__label",
            text: name,
          }),
          createElement("div", {
            className: "http-attachment__value",
            text: value,
          }),
        ],
      })
    : null;

const renderPairsTable = (title: string, pairs: HttpPair[]) =>
  pairs.length
    ? createElement("div", {
        className: "http-attachment__subsection",
        children: [
          createElement("h4", {
            className: "http-attachment__subsection-title",
            text: title,
          }),
          createElement("table", {
            className: "http-attachment__table",
            children: createElement("tbody", {
              children: pairs.map(({ masked, name, value }) =>
                createElement("tr", {
                  children: [
                    createElement("td", {
                      className: "http-attachment__pair-name",
                      text: name,
                    }),
                    createElement("td", {
                      className: masked
                        ? "http-attachment__pair-value http-attachment__pair-value_masked"
                        : "http-attachment__pair-value",
                      children: renderPairValue({ masked, name, value }),
                    }),
                  ],
                }),
              ),
            }),
          }),
        ],
      })
    : null;

const cookieAttributes = (cookie: HttpCookie) =>
  [
    cookie.domain ? `Domain=${cookie.domain}` : null,
    cookie.path ? `Path=${cookie.path}` : null,
    cookie.expires ? `Expires=${cookie.expires}` : null,
    typeof cookie.maxAge === "number" ? `Max-Age=${cookie.maxAge}` : null,
    cookie.sameSite ? `SameSite=${cookie.sameSite}` : null,
    cookie.secure ? "Secure" : null,
    cookie.httpOnly ? "HttpOnly" : null,
  ]
    .filter((value): value is string => Boolean(value))
    .join("; ");

const renderCookiesTable = (title: string, cookies: HttpCookie[]) =>
  cookies.length
    ? createElement("div", {
        className: "http-attachment__subsection",
        children: [
          createElement("h4", {
            className: "http-attachment__subsection-title",
            text: title,
          }),
          createElement("table", {
            className: "http-attachment__table",
            children: createElement("tbody", {
              children: cookies.map((cookie) =>
                createElement("tr", {
                  children: [
                    createElement("td", {
                      className: "http-attachment__pair-name",
                      text: cookie.name,
                    }),
                    createElement("td", {
                      className: cookie.masked
                        ? "http-attachment__pair-value http-attachment__pair-value_masked"
                        : "http-attachment__pair-value",
                      children: renderPairValue(cookie),
                    }),
                    createElement("td", {
                      className: "http-attachment__cookie-attrs",
                      text: cookieAttributes(cookie),
                    }),
                  ],
                }),
              ),
            }),
          }),
        ],
      })
    : null;

const renderInformationalResponses = (responses: HttpInformationalResponse[]) =>
  responses.length
    ? createElement("div", {
        className: "http-attachment__subsection",
        children: [
          createElement("h4", {
            className: "http-attachment__subsection-title",
            text: "Informational responses",
          }),
          createElement("table", {
            className: "http-attachment__table",
            children: createElement("tbody", {
              children: responses.map(({ headers, reason, status }) =>
                createElement("tr", {
                  children: [
                    createElement("td", {
                      className: "http-attachment__pair-name",
                      text: [status, reason].filter(Boolean).join(" "),
                    }),
                    createElement("td", {
                      className: "http-attachment__pair-value",
                      children: renderInlinePairs(headers),
                    }),
                  ],
                }),
              ),
            }),
          }),
        ],
      })
    : null;

const bodyMeta = (body: HttpBody) =>
  [
    body.contentType || "body",
    body.encoding && body.encoding !== "utf8" ? body.encoding : null,
    typeof body.size === "number" ? `${body.size} bytes` : null,
    body.truncated ? "truncated" : null,
  ]
    .filter((value): value is string => Boolean(value))
    .join(" | ");

const renderBody = (body: HttpBody | null) => {
  if (!body) {
    return null;
  }

  const canRenderBody = body.hasValue;
  const hasStructuredBody = Boolean(body.form.length || body.parts.length || body.stream);

  return createElement("div", {
    className: "http-attachment__subsection http-attachment__body",
    children: [
      createElement("div", {
        className: "http-attachment__body-toolbar",
        children: createElement("div", {
          className: "http-attachment__body-meta",
          text: bodyMeta(body),
        }),
      }),
      canRenderBody || hasStructuredBody
        ? renderBodyContent(body)
        : createElement("div", {
            className: "http-attachment__body-message",
            text: "No body captured.",
          }),
    ],
  });
};

const renderSectionHeader = (
  title: "Request" | "Response" | "Error",
  action: HTMLElement | null = null,
) =>
  createElement("div", {
    className: "http-attachment__section-header",
    children: [
      createElement("h3", {
        className: "http-attachment__section-title",
        text: title,
      }),
      action,
    ],
  });

const renderRequestSection = (request: HttpRequest) =>
  createElement("section", {
    className: "http-attachment__section",
    children: [
      renderSectionHeader("Request", renderBodyDownload(request.body, "request")),
      request.httpVersion
        ? createElement("div", {
            className: "http-attachment__meta",
            children: renderMetaRow("HTTP version", request.httpVersion),
          })
        : null,
      renderPairsTable("Query", request.query),
      renderPairsTable("Headers", request.headers),
      renderCookiesTable("Cookies", request.cookies),
      renderPairsTable("Trailers", request.trailers),
      renderBody(request.body),
    ],
  });

const renderResponseSection = (response: HttpResponse | null) => {
  if (!response) {
    return createElement("section", {
      className: "http-attachment__section",
      children: [
        renderSectionHeader("Response"),
        createElement("div", {
          className: "http-attachment__empty",
          text: "No response captured.",
        }),
      ],
    });
  }

  const statusLine = [response.status, response.reason].filter(Boolean).join(" ");

  return createElement("section", {
    className: "http-attachment__section",
    children: [
      renderSectionHeader("Response", renderBodyDownload(response.body, "response")),
      createElement("div", {
        className: "http-attachment__meta",
        children: [
          renderMetaRow("Status", statusLine),
          renderMetaRow("HTTP version", response.httpVersion),
        ],
      }),
      renderInformationalResponses(response.informationalResponses),
      renderPairsTable("Headers", response.headers),
      renderCookiesTable("Cookies", response.cookies),
      renderPairsTable("Trailers", response.trailers),
      renderBody(response.body),
    ],
  });
};

const renderErrorSection = (error: HttpError | null) => {
  if (!error) {
    return null;
  }

  return createElement("section", {
    className: "http-attachment__section",
    children: [
      renderSectionHeader("Error"),
      createElement("div", {
        className: "http-attachment__meta",
        children: [renderMetaRow("Name", error.name), renderMetaRow("Message", error.message)],
      }),
      error.stack
        ? createElement("div", {
            className: "http-attachment__subsection",
            children: [
              createElement("h4", {
                className: "http-attachment__subsection-title",
                text: "Stack",
              }),
              createElement("div", {
                className: "http-attachment__body-content",
                children: renderBodyFallbackCode(error.stack),
              }),
            ],
          })
        : null,
    ],
  });
};

const renderSummary = (
  request: HttpRequest,
  response: HttpResponse | null,
  duration: HttpDuration | null,
) => {
  const statusLine = response ? [response.status, response.reason].filter(Boolean).join(" ") : "";

  return createElement("div", {
    className: "http-attachment__summary",
    children: [
      createElement("span", {
        className: "http-attachment__method",
        text: request.method,
      }),
      createElement("span", {
        className: "http-attachment__url",
        text: getRequestTarget(request),
      }),
      statusLine
        ? createElement("span", {
            className: "http-attachment__status",
            text: statusLine,
          })
        : null,
      duration?.durationMs
        ? createElement("span", {
            className: "http-attachment__duration",
            text: duration.durationMs,
          })
        : null,
    ],
  });
};

const createHttpAttachmentContent = (payload: HttpExchangePayload) => {
  const normalized = normalizePayload(payload);
  const el = defineMountableElement(document.createElement("div"), {
    onMaskedValueHover(event: Event) {
      tooltip.show(MASKED_VALUE_TOOLTIP, event.currentTarget as HTMLElement);
    },
    onTooltipLeave() {
      tooltip.hide();
    },
  });
  const tooltip = new TooltipView({ position: "bottom" });
  let releaseEvents = () => {};

  Object.assign(el, {
    render() {
      releaseEvents();
      el.className = "http-attachment";

      if (!normalized.request) {
        el.replaceChildren(
          createElement("div", {
            className: "http-attachment__empty",
            text: "Invalid HTTP Exchange attachment: request is missing.",
          }),
        );
        return el;
      }

      el.replaceChildren(
        ...[
          renderSummary(normalized.request, normalized.response, normalized.duration),
          renderRequestSection(normalized.request),
          renderResponseSection(normalized.response),
          renderErrorSection(normalized.error),
        ].filter((node): node is HTMLElement => node !== null),
      );

      releaseEvents = bindDelegatedEvents({
        root: el,
        events: {
          "mouseenter [data-http-masked-value]": "onMaskedValueHover",
          "focusin [data-http-masked-value]": "onMaskedValueHover",
          "mouseleave [data-http-masked-value]": "onTooltipLeave",
          "focusout [data-http-masked-value]": "onTooltipLeave",
        },
        context: el,
      });

      return el;
    },
    destroy() {
      releaseEvents();
      tooltip.hide();
      el.remove();
    },
  });

  return el;
};

export const HttpAttachmentView = (options: HttpAttachmentOptions) => {
  const el = defineMountableElement(document.createElement("div"), {});
  let requestId = 0;
  let subView: Mountable | null = null;

  const destroySubView = () => {
    if (!subView) {
      return;
    }

    destroyMountable(subView);
    subView = null;
  };

  const mountSubView = (view: Mountable) => {
    const container = el.querySelector(".http-attachment-view");
    if (!(container instanceof Element)) {
      return;
    }

    destroySubView();
    subView = attachMountable(container, view);
  };

  Object.assign(el, {
    render() {
      destroySubView();
      el.replaceChildren(createElement("div", { className: "http-attachment-view" }));
      const currentRequestId = ++requestId;
      void mountAsyncView({
        createError: (error) => createReportLoadErrorView(error),
        createSuccess: createHttpAttachmentContent,
        load: () => fetchReportJson<HttpExchangePayload>(options.sourceUrl),
        mount: mountSubView,
        shouldIgnore: () => currentRequestId !== requestId || !el.isConnected,
      });
      return el;
    },
    attachToDom() {
      subView?.attachToDom?.();
    },
    detachFromDom() {
      subView?.detachFromDom?.();
    },
    destroy() {
      el.detachFromDom?.();
      requestId += 1;
      destroySubView();
      el.remove();
    },
  });

  return el;
};
