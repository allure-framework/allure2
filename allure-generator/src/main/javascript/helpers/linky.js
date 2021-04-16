import { SafeString } from "handlebars/runtime";

const URL_REGEXP = /^(\w)+:\/\/.*/;

export default function(text) {
  return URL_REGEXP.test(text)
    ? new SafeString(`<a href="${text}"  class="link" target="_blank">${text}</a>`)
    : text;
}
