const SCHEME_PATTERN = /^[a-z][a-z\d+\-.]*:/i;

const ALLOWED_NAVIGATION_PROTOCOLS = new Set(["ftp:", "http:", "https:", "mailto:"]);

const decodeHtmlEntities = (value: string): string => {
  if (typeof DOMParser === "undefined") {
    return value;
  }

  return new DOMParser().parseFromString(value, "text/html").documentElement.textContent || value;
};

const stripIgnoredSchemeCharacters = (value: string) =>
  Array.from(value)
    .filter((char) => {
      const code = char.charCodeAt(0);
      return !/\s/u.test(char) && code > 0x20 && code !== 0x7f;
    })
    .join("");

const normalizeCandidate = (value: string) =>
  stripIgnoredSchemeCharacters(decodeHtmlEntities(value.trim()));

const hasExplicitScheme = (value: string) => SCHEME_PATTERN.test(value);

const hasIgnoredUrlCharacters = (value: string) => stripIgnoredSchemeCharacters(value) !== value;

const isProtocolRelativeUrl = (value: string) => value.startsWith("//");

const isRelativeUrl = (value: string) => {
  if (!value) {
    return false;
  }

  if (hasExplicitScheme(value) || isProtocolRelativeUrl(value)) {
    return false;
  }

  return (
    value.startsWith("#") ||
    value.startsWith("?") ||
    value.startsWith("/") ||
    value.startsWith("./") ||
    value.startsWith("../") ||
    !value.startsWith("\\")
  );
};

const resolveProtocol = (value: string): string | null => {
  if (hasExplicitScheme(value)) {
    return value.match(SCHEME_PATTERN)?.[0].toLowerCase() || null;
  }

  if (!isProtocolRelativeUrl(value)) {
    return null;
  }

  try {
    return new URL(value, "https://allure.local").protocol.toLowerCase();
  } catch {
    return null;
  }
};

const sanitizeNormalizedNavigationUrl = (
  rawValue: string,
  normalizedValue: string,
): string | null => {
  if (!normalizedValue) {
    return null;
  }

  if (isRelativeUrl(normalizedValue)) {
    return rawValue;
  }

  const protocol = resolveProtocol(normalizedValue);
  if (!protocol) {
    return null;
  }

  return ALLOWED_NAVIGATION_PROTOCOLS.has(protocol) ? rawValue : null;
};

export const sanitizeNavigationUrl = (value: unknown): string | null => {
  if (value === null || typeof value === "undefined") {
    return null;
  }

  const rawValue = String(value).trim();
  if (!rawValue) {
    return null;
  }

  return sanitizeNormalizedNavigationUrl(rawValue, normalizeCandidate(rawValue));
};

export const sanitizeDetectedNavigationUrl = (value: unknown): string | null => {
  if (value === null || typeof value === "undefined") {
    return null;
  }

  const rawValue = String(value).trim();
  if (!rawValue) {
    return null;
  }

  const decodedValue = decodeHtmlEntities(rawValue);
  if (hasIgnoredUrlCharacters(decodedValue)) {
    return null;
  }

  const isWwwUrl = /^www\./i.test(rawValue);
  const normalizedValue = isWwwUrl ? `https://${decodedValue}` : decodedValue;
  if (!hasExplicitScheme(normalizedValue)) {
    return null;
  }

  try {
    new URL(normalizedValue);
  } catch {
    return null;
  }

  const href = isWwwUrl ? `https://${rawValue}` : rawValue;

  return sanitizeNormalizedNavigationUrl(href, normalizedValue);
};
