import TimelineLayout from "./TimelineLayout";

allure.api.addTab("timeline", {
  title: "tab.timeline.name",
  icon: "fa fa-clock-o",
  route: "timeline",
  onEnter: (...routeParams) =>
    new TimelineLayout({
      ...routeParams,
      url: "data/timeline.json",
    }),
});
