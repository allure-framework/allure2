import { defineWidgetDescriptor } from "../../core/registry/types.mts";
import { createWidgetDataLoader } from "../../core/services/widgetData.mts";
import { createWidgetStatusFactory } from "../../shared/ui/WidgetStatusView.mts";
import { createTreeTab } from "../tree/createTreeTab.mts";

type WidgetInputOf<TWidget extends (options?: never) => unknown> = NonNullable<
  NonNullable<Parameters<TWidget>[0]>["data"]
>;

const SuitesWidgetView = createWidgetStatusFactory({
  title: "widget.suites.name",
  baseUrl: "suites",
  showLinks: true,
});

export const suitesTab = createTreeTab({
  baseUrl: "suites",
  title: "tab.suites.name",
  icon: "lineFilesFolder",
  url: "data/suites.json",
  csvUrl: "data/suites.csv",
});

export const suitesWidgetDescriptor = defineWidgetDescriptor({
  widgetName: "suites",
  create: SuitesWidgetView,
  load: createWidgetDataLoader<WidgetInputOf<typeof SuitesWidgetView>>("suites"),
});
