import { createElement, createFragment } from "../shared/dom.mts";
import { sanitizeNavigationUrl } from "../shared/url.mts";

const URL_REGEXP = /((?:(https?:\/\/|ftp:\/\/|mailto:)|www\.)\S+?)(\s|"|'|\)|]|}|&#62|$)/gm;

export const createTextWithLinksFragment = (text: unknown) => {
  const value = text == null ? "" : String(text);
  const fragment = createFragment();

  if (!value) {
    return fragment;
  }

  URL_REGEXP.lastIndex = 0;
  let lastIndex = 0;
  let match: RegExpExecArray | null;

  while ((match = URL_REGEXP.exec(value)) !== null) {
    const [matchedText, urlFullText, urlProtocol = "", terminalSymbol = ""] = match;
    const matchIndex = match.index;
    const fullEnd = matchIndex + matchedText.length;

    if (lastIndex < matchIndex) {
      fragment.append(value.slice(lastIndex, matchIndex));
    }

    const href = urlProtocol ? urlFullText : `https://${urlFullText}`;
    const safeHref = sanitizeNavigationUrl(href);

    if (!safeHref) {
      fragment.append(urlFullText);
    } else {
      fragment.append(
        createElement("a", {
          attrs: {
            href: safeHref,
            rel: "noopener noreferrer",
            target: "_blank",
          },
          className: "link",
          text: urlFullText,
        }),
      );
    }

    if (terminalSymbol) {
      fragment.append(terminalSymbol);
    }

    lastIndex = fullEnd;

    if (matchedText.length === 0) {
      break;
    }
  }

  if (lastIndex < value.length) {
    fragment.append(value.slice(lastIndex));
  }

  return fragment;
};
