import i18next from "../utils/translation";

export default function translate(key, options) {
  return i18next.t(key, options ? options.hash : {});
}
