import { createIconElement } from "../shared/icon/index.mts";
import type { TranslationKey } from "../core/i18n/types.mts";
import translate from "./t.mts";

type IconDefinition = {
  className?: string;
  icon: import("../shared/icon/index.mts").IconName;
  tooltip: TranslationKey;
};

type AllureIconOptions = {
  className?: string;
  noTooltip?: boolean;
  size?: import("../shared/icon/index.mts").IconSize;
};

const hasHashOptions = (
  value: AllureIconOptions | { hash?: AllureIconOptions },
): value is { hash?: AllureIconOptions } => "hash" in value;

const icons: Record<string, IconDefinition> = {
  flaky: {
    icon: "lineIconBomb2",
    tooltip: "marks.flaky",
  },
  newFailed: {
    icon: "lineAlertsRegressed",
    tooltip: "marks.newFailed",
  },
  newBroken: {
    icon: "lineAlertsMalfunctioned",
    tooltip: "marks.newBroken",
  },
  newPassed: {
    icon: "lineAlertsFixed",
    tooltip: "marks.newPassed",
  },
  retriesStatusChange: {
    icon: "lineArrowsRefreshCcw1",
    tooltip: "marks.retriesStatusChange",
  },
  failed: {
    className: "text_status_failed",
    icon: "solidXCircle",
    tooltip: "status.failed",
  },
  broken: {
    className: "text_status_broken",
    icon: "solidAlertCircle",
    tooltip: "status.broken",
  },
  passed: {
    className: "text_status_passed",
    icon: "solidCheckCircle",
    tooltip: "status.passed",
  },
  skipped: {
    className: "text_status_skipped",
    icon: "solidMinusCircle",
    tooltip: "status.skipped",
  },
  unknown: {
    className: "text_status_unknown",
    icon: "solidHelpCircle",
    tooltip: "status.unknown",
  },
};

const getIconDefinition = (value: string) => icons[value];

const resolveIconOptions = (opts: AllureIconOptions | { hash?: AllureIconOptions } = {}) =>
  hasHashOptions(opts) ? opts.hash || {} : opts;

export const createAllureIconElement = (
  value: string,
  opts: AllureIconOptions | { hash?: AllureIconOptions } = {},
) => {
  const resolvedOptions = resolveIconOptions(opts);
  const { className = "", noTooltip = false, size = "s" } = resolvedOptions;
  const icon = getIconDefinition(value);

  if (!icon) {
    return null;
  }

  return createIconElement(icon.icon, {
    attributes: noTooltip ? {} : { "data-tooltip": translate(icon.tooltip) },
    className: `${icon.className || ""} ${className}`.trim(),
    size,
  });
};
