type SafeUrlKind = "navigation" | "resource";

const SCHEME_PATTERN = /^[a-z][a-z\d+\-.]*:/i;

const ALLOWED_PROTOCOLS: Record<SafeUrlKind, Set<string>> = {
  navigation: new Set(["ftp:", "http:", "https:", "mailto:"]),
  resource: new Set(["blob:", "data:", "file:", "ftp:", "http:", "https:"]),
};

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

const sanitizeUrl = (value: unknown, kind: SafeUrlKind): string | null => {
  if (value === null || typeof value === "undefined") {
    return null;
  }

  const rawValue = String(value).trim();
  if (!rawValue) {
    return null;
  }

  const normalized = normalizeCandidate(rawValue);
  if (!normalized) {
    return null;
  }

  if (isRelativeUrl(normalized)) {
    return rawValue;
  }

  const protocol = resolveProtocol(normalized);
  if (!protocol) {
    return null;
  }

  return ALLOWED_PROTOCOLS[kind].has(protocol) ? rawValue : null;
};

export const sanitizeNavigationUrl = (value: unknown) => sanitizeUrl(value, "navigation");

export const sanitizeResourceUrl = (value: unknown) => sanitizeUrl(value, "resource");
