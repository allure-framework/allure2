import "./HistoryView.scss";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import { renderHistory } from "./renderHistory.mts";

type TestResult = import("../../../types/report.mts").TestResult;

type HistoryOptions = {
  data: TestResult;
};

type HistoryData = NonNullable<NonNullable<TestResult["extra"]>["history"]>;

const formatNumber = (n: number): string => (Math.floor(n * 100) / 100).toString();

const getSuccessRate = (history: HistoryData | null | undefined): string => {
  if (!history || !history.statistic || !history.statistic.total) {
    return "unknown";
  }
  const { passed, total } = history.statistic;
  return `${formatNumber(((passed || 0) / total) * 100)}%`;
};

const HistoryView = (options: HistoryOptions) => {
  const el = defineMountableElement(document.createElement("div"), {});

  Object.assign(el, {
    render() {
      const extra = options?.data.extra;
      const history = extra?.history || null;
      el.className = "test-result-history";
      el.replaceChildren(
        renderHistory({
          cls: "test-result-history",
          history,
          successRate: getSuccessRate(history),
        }),
      );
      return el;
    },
    destroy() {
      el.remove();
    },
  });

  return el;
};

export default HistoryView;
