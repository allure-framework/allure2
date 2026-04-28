import { values } from "../utils/statuses.mts";
import { createElement } from "../shared/dom.mts";

type Statistic = import("../types/report.mts").Statistic;

export default function statusBar(statistic: Statistic | null | undefined): HTMLDivElement {
  return createElement("div", {
    className: "bar",
    children: values
      .map((status) => {
        const count = statistic?.[status] ?? 0;
        return count === 0
          ? null
          : createElement("div", {
              attrs: { style: `flex-grow: ${count}` },
              className: `bar__fill bar__fill_status_${status}`,
              text: count,
            });
      })
      .filter(Boolean),
  });
}
