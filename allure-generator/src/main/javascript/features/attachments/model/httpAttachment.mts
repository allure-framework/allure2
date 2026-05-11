const HTTP_EXCHANGE_ATTACHMENT_MIME = "application/vnd.allure.http+json";
const HTTP_EXCHANGE_ATTACHMENT_MIME_ALIAS = "application/vnd.allure.http";
export const HTTP_EXCHANGE_ATTACHMENT_MIME_TYPES = [
  HTTP_EXCHANGE_ATTACHMENT_MIME,
  HTTP_EXCHANGE_ATTACHMENT_MIME_ALIAS,
];
export const HTTP_EXCHANGE_REDACTED_VALUE = "__ALLURE_REDACTED__";

export type HttpExchangeSchemaVersion = 1;
export type HttpExchangeBodyEncoding = "utf8" | "base64" | (string & {});

export type HttpExchangeNameValue = {
  name: string;
  value: string;
};

export type HttpExchangeCookie = HttpExchangeNameValue & {
  domain?: string;
  expires?: string;
  httpOnly?: boolean;
  maxAge?: number;
  path?: string;
  sameSite?: string;
  secure?: boolean;
};

export type HttpExchangeStream = {
  type?: string;
  complete?: boolean;
  chunkCount?: number;
};

export type HttpExchangeBodyPart = {
  name?: string;
  fileName?: string;
  headers?: HttpExchangeNameValue[];
  contentType?: string;
  encoding?: HttpExchangeBodyEncoding;
  value?: string;
  size?: number;
  truncated?: boolean;
};

export type HttpExchangeBody = {
  contentType?: string;
  encoding?: HttpExchangeBodyEncoding;
  value?: string;
  size?: number;
  truncated?: boolean;
  form?: HttpExchangeNameValue[];
  parts?: HttpExchangeBodyPart[];
  stream?: HttpExchangeStream;
};

export type HttpExchangeRequest = {
  method: string;
  url: string;
  httpVersion?: string;
  cookies?: HttpExchangeCookie[];
  headers?: HttpExchangeNameValue[];
  query?: HttpExchangeNameValue[];
  body?: HttpExchangeBody;
  trailers?: HttpExchangeNameValue[];
};

export type HttpExchangeInformationalResponse = {
  status?: number;
  statusText?: string;
  headers?: HttpExchangeNameValue[];
};

export type HttpExchangeResponse = {
  status?: number;
  statusText?: string;
  httpVersion?: string;
  cookies?: HttpExchangeCookie[];
  headers?: HttpExchangeNameValue[];
  body?: HttpExchangeBody;
  trailers?: HttpExchangeNameValue[];
  informationalResponses?: HttpExchangeInformationalResponse[];
};

export type HttpExchangeError = {
  name?: string;
  message?: string;
  stack?: string;
};

export type HttpExchangePayload = {
  schemaVersion: HttpExchangeSchemaVersion;
  request: HttpExchangeRequest;
  response?: HttpExchangeResponse;
  error?: HttpExchangeError;
  start?: number;
  stop?: number;
};
