import { escapeExpression, SafeString } from "handlebars/runtime";

const URL_REGEXP = /^(\w)+:\/\/.*/;

export default function (text) {
  if (!URL_REGEXP.test(text)) {
    return text;
  }

  const safeText = escapeExpression(text);

  return new SafeString(
    `<a href="${safeText}" class="link" target="_blank" rel="noopener noreferrer">${safeText}</a>`
  );
}
