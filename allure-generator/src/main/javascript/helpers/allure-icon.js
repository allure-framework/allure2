import { SafeString } from "handlebars/runtime";
import translate from "./t";

const icons = {
  flaky: {
    className: "fa fa-bomb",
    tooltip: "marks.flaky",
  },
  newFailed: {
    className: "fa fa-times-circle",
    tooltip: "marks.newFailed",
  },
  newBroken: {
    className: "fa fa-exclamation-circle",
    tooltip: "marks.newBroken",
  },
  newPassed: {
    className: "fa fa-check-circle",
    tooltip: "marks.newPassed",
  },
  retriesStatusChange: {
    className: "fa fa-refresh",
    tooltip: "marks.retriesStatusChange",
  },
  failed: {
    className: "fa fa-times-circle fa-fw text_status_failed",
    tooltip: "status.failed",
  },
  broken: {
    className: "fa fa-exclamation-circle fa-fw text_status_broken",
    tooltip: "status.broken",
  },
  passed: {
    className: "fa fa-check-circle fa-fw text_status_passed",
    tooltip: "status.passed",
  },
  skipped: {
    className: "fa fa-minus-circle fa-fw text_status_skipped",
    tooltip: "status.skipped",
  },
  unknown: {
    className: "fa fa-question-circle fa-fw text_status_unknown",
    tooltip: "status.unknown",
  },
};

export default function(value, opts) {
  const {
    hash: { extraClasses = "", noTooltip = false },
  } = opts;
  const icon = icons[value];
  return icon
    ? new SafeString(
        `<span class="${icon.className} ${extraClasses}" ${
          noTooltip ? "" : `data-tooltip="${translate(icon.tooltip)}"`
        }></span>`,
      )
    : "";
}
