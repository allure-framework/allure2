import pad from "underscore.string/pad";

export default function(date, showMilliseconds) {
  if (!date) {
    return "unknown";
  }
  if (!(date instanceof Date)) {
    date = new Date(date);
  }
  if (typeof showMilliseconds !== "boolean") {
    showMilliseconds = false;
  }
  return [
    date.getHours(),
    pad(date.getMinutes(), 2, "0"),
    pad(date.getSeconds(), 2, "0") + (showMilliseconds ? `.${date.getMilliseconds()}` : ""),
  ].join(":");
}
