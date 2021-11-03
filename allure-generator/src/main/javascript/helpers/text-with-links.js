import { SafeString } from "handlebars/runtime";

const URL_REGEXP = /((?:(https?:\/\/|ftp:\/\/)|(?:(?:[0-9]{1,3}\.){3}[0-9]{1,3}|www\.?))\S+?)(\s|"|'|\)|]|$)/gm;

const encodeHTMLEntities = (rawString) =>
  rawString.replace(/[\u00A0-\u9999<>&]/gim, (i) => `&#${i.charCodeAt(0)};`);

export default function(text) {
  const hasUrl = text !== undefined && text.match(URL_REGEXP);
  return hasUrl
    ? new SafeString(
        encodeHTMLEntities(text).replace(
          URL_REGEXP,
          (_, urlFullText, urlProtocol, terminalSymbol) => {
            return ` <a class="link" target="_blank" href="${
              urlProtocol ? urlFullText : `https://${urlFullText}`
            }">${urlFullText}</a>${terminalSymbol} `;
          },
        ),
      )
    : text;
}
