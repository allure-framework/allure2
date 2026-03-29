import { escapeExpression, SafeString } from "handlebars/runtime";

const URL_REGEXP =
  /((?:(https?:\/\/|ftp:\/\/|mailto:)|www\.)\S+?)(\s|"|'|\)|]|}|&#62|$)/gm;

export default function (text) {
  const hasUrl = text !== undefined && text.match(URL_REGEXP);

  if (!hasUrl) {
    return text;
  }

  const escapedText = escapeExpression(text);

  return new SafeString(
    escapedText.replace(
      URL_REGEXP,
      (_, urlFullText, urlProtocol, terminalSymbol) => {
        const href = urlProtocol ? urlFullText : `https://${urlFullText}`;

        // eslint-disable-next-line max-len
        return `<a class="link" target="_blank" href="${href}" rel="noopener noreferrer">${urlFullText}</a>${terminalSymbol} `;
      },
    ),
  );
}
