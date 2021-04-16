import WidgetStatusView from "../../components/widget-status/WidgetStatusView";

allure.api.addWidget(
  "widgets",
  "categories",
  WidgetStatusView.extend({
    rowTag: "a",
    title: "widget.categories.name",
    baseUrl: "categories",
    showLinks: true,
  }),
);
