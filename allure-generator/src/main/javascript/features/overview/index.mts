import { categoriesWidgetDescriptor } from "../categories/index.mts";
import { behaviorsWidgetDescriptor } from "../behaviors/index.mts";
import { suitesWidgetDescriptor } from "../suites/index.mts";
import { defineWidgetDescriptor } from "../../core/registry/types.mts";
import { createWidgetDataLoader } from "../../core/services/widgetData.mts";
import GraphLayout from "./GraphLayout.mts";
import { loadTrendData } from "./model/widgetData.mts";
import OverviewLayout from "./OverviewLayout.mts";
import CategoriesTrendWidgetView from "./widgets/CategoriesTrendWidgetView.mts";
import DurationTrendWidgetView from "./widgets/DurationTrendWidgetView.mts";
import DurationWidgetView from "./widgets/DurationWidgetView.mts";
import EnvironmentWidget from "./widgets/EnvironmentWidget.mts";
import ExecutorsWidgetView from "./widgets/ExecutorsWidgetView.mts";
import HistoryTrendWidgetView from "./widgets/HistoryTrendWidgetView.mts";
import RetryTrendWidgetView from "./widgets/RetryTrendWidgetView.mts";
import SeverityChartView from "./widgets/SeverityWidgetView.mts";
import StatusChartView from "./widgets/StatusWidgetView.mts";
import SummaryWidgetView from "./widgets/SummaryWidgetView.mts";

type TabDescriptor = import("../../core/registry/types.mts").TabDescriptor;
type WidgetsByTab = import("../../core/registry/types.mts").WidgetsByTab;
type WidgetInputOf<TWidget extends (options?: never) => unknown> = NonNullable<
  NonNullable<Parameters<TWidget>[0]>["data"]
>;

const loadTrendDataAs =
  <TData,>(name: string) =>
  () =>
    loadTrendData(name) as Promise<TData>;

export const overviewTab: TabDescriptor = {
  tabName: "",
  title: "tab.overview.name",
  icon: "lineGeneralHomeLine",
  route: "",
  onEnter: () =>
    OverviewLayout({
      tabName: "tab.overview.name",
    }),
};

export const graphTab: TabDescriptor = {
  tabName: "graph",
  title: "tab.graph.name",
  icon: "lineChartsBarChartSquare",
  route: "graph",
  onEnter: () => GraphLayout(),
};

export const widgetsByTab: WidgetsByTab = {
  graph: [
    defineWidgetDescriptor({
      widgetName: "status-chart",
      create: StatusChartView,
      load: createWidgetDataLoader<WidgetInputOf<typeof StatusChartView>>("status-chart"),
    }),
    defineWidgetDescriptor({
      widgetName: "severity",
      create: SeverityChartView,
      load: createWidgetDataLoader<WidgetInputOf<typeof SeverityChartView>>("severity"),
    }),
    defineWidgetDescriptor({
      widgetName: "duration",
      create: DurationWidgetView,
      load: createWidgetDataLoader<WidgetInputOf<typeof DurationWidgetView>>("duration"),
    }),
    defineWidgetDescriptor({
      widgetName: "duration-trend",
      create: DurationTrendWidgetView,
      load: loadTrendDataAs<WidgetInputOf<typeof DurationTrendWidgetView>>("duration-trend"),
    }),
    defineWidgetDescriptor({
      widgetName: "retry-trend",
      create: RetryTrendWidgetView,
      load: loadTrendDataAs<WidgetInputOf<typeof RetryTrendWidgetView>>("retry-trend"),
    }),
    defineWidgetDescriptor({
      widgetName: "categories-trend",
      create: CategoriesTrendWidgetView,
      load: loadTrendDataAs<WidgetInputOf<typeof CategoriesTrendWidgetView>>("categories-trend"),
    }),
    defineWidgetDescriptor({
      widgetName: "history-trend",
      create: HistoryTrendWidgetView,
      load: loadTrendDataAs<WidgetInputOf<typeof HistoryTrendWidgetView>>("history-trend"),
    }),
  ],
  widgets: [
    defineWidgetDescriptor({
      widgetName: "summary",
      create: SummaryWidgetView,
      load: createWidgetDataLoader<WidgetInputOf<typeof SummaryWidgetView>>("summary"),
    }),
    defineWidgetDescriptor({
      widgetName: "history-trend",
      create: HistoryTrendWidgetView,
      load: loadTrendDataAs<WidgetInputOf<typeof HistoryTrendWidgetView>>("history-trend"),
    }),
    suitesWidgetDescriptor,
    categoriesWidgetDescriptor,
    defineWidgetDescriptor({
      widgetName: "environment",
      create: EnvironmentWidget,
      load: createWidgetDataLoader<WidgetInputOf<typeof EnvironmentWidget>>("environment"),
    }),
    defineWidgetDescriptor({
      widgetName: "executors",
      create: ExecutorsWidgetView,
      load: createWidgetDataLoader<WidgetInputOf<typeof ExecutorsWidgetView>>("executors"),
    }),
    behaviorsWidgetDescriptor,
  ],
};
