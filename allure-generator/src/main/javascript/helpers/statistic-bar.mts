import { createElement, createFragment } from "../shared/dom.mts";
import { values } from "../utils/statuses.mts";

type Statistic = import("../types/report.mts").Statistic;

export const createStatisticBarFragment = (statistic: Statistic | null | undefined) => {
  const fragment = createFragment();

  values.forEach((status) => {
    const count = statistic?.[status] ?? 0;
    if (count === 0) {
      return;
    }

    fragment.append(
      createElement("span", {
        className: `label label_status_${status}`,
        text: count,
      }),
      " ",
    );
  });

  return fragment;
};
