import { defineWidgetDescriptor } from "../../core/registry/types.mts";
import { createWidgetDataLoader } from "../../core/services/widgetData.mts";
import { createWidgetStatusFactory } from "../../shared/ui/WidgetStatusView.mts";
import { createTreeTab } from "../tree/createTreeTab.mts";

type WidgetInputOf<TWidget extends (options?: never) => unknown> = NonNullable<
  NonNullable<Parameters<TWidget>[0]>["data"]
>;

const CategoriesWidgetView = createWidgetStatusFactory({
  title: "widget.categories.name",
  baseUrl: "categories",
  showLinks: true,
});

export const categoriesTab = createTreeTab({
  baseUrl: "categories",
  title: "tab.categories.name",
  icon: "lineHelpersFlag",
  url: "data/categories.json",
  csvUrl: "data/categories.csv",
});

export const categoriesWidgetDescriptor = defineWidgetDescriptor({
  widgetName: "categories",
  create: CategoriesWidgetView,
  load: createWidgetDataLoader<WidgetInputOf<typeof CategoriesWidgetView>>("categories"),
});
