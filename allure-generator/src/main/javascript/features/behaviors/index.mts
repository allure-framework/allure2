import { defineWidgetDescriptor } from "../../core/registry/types.mts";
import { createWidgetDataLoader } from "../../core/services/widgetData.mts";
import { createWidgetStatusFactory } from "../../shared/ui/WidgetStatusView.mts";
import { createTreeTab } from "../tree/createTreeTab.mts";

type WidgetInputOf<TWidget extends (options?: never) => unknown> = NonNullable<
  NonNullable<Parameters<TWidget>[0]>["data"]
>;

const BehaviorsWidgetView = createWidgetStatusFactory({
  title: "widget.behaviors.name",
  baseUrl: "behaviors",
  showAllKey: "widget.behaviors.showAll",
  showLinks: true,
});

export const behaviorsTab = createTreeTab({
  baseUrl: "behaviors",
  title: "tab.behaviors.name",
  icon: "lineDevDataflow3",
  url: "data/behaviors.json",
  csvUrl: "data/behaviors.csv",
});

export const behaviorsWidgetDescriptor = defineWidgetDescriptor({
  widgetName: "behaviors",
  create: BehaviorsWidgetView,
  load: createWidgetDataLoader<WidgetInputOf<typeof BehaviorsWidgetView>>("behaviors"),
});
