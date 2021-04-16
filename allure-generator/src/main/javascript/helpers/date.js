import i18next from "../utils/translation";

export default function(date) {
  if (!date) {
    return "unknown";
  }
  if (!(date instanceof Date)) {
    date = new Date(date);
  }
  return new Intl.DateTimeFormat(i18next.language).format(date);
}
