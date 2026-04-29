import pad from "../shared/utils/pad.mts";

export default function formatTime(
  date: Date | number | string | null | undefined,
  showMilliseconds = false,
) {
  if (!date) {
    return "unknown";
  }
  const nextDate = date instanceof Date ? date : new Date(date);
  return [
    nextDate.getHours(),
    pad(nextDate.getMinutes(), 2, "0"),
    pad(nextDate.getSeconds(), 2, "0") + (showMilliseconds ? `.${nextDate.getMilliseconds()}` : ""),
  ].join(":");
}
