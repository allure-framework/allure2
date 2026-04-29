import i18next from "../core/i18n/index.mts";

export default function formatDate(
  date: Date | number | string | null | undefined,
  options: Intl.DateTimeFormatOptions = {},
) {
  if (!date) {
    return "unknown";
  }
  const nextDate = date instanceof Date ? date : new Date(date);
  if (Number.isNaN(nextDate.valueOf())) {
    return "unknown";
  }

  return new Intl.DateTimeFormat(i18next.language, options).format(nextDate);
}
