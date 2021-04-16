import OverviewLayout from "../../layouts/overview/OverviewLayout";

allure.api.addTab("", {
  title: "tab.overview.name",
  icon: "fa fa-home",
  route: "",
  onEnter: () =>
    new OverviewLayout({
      tabName: "tab.overview.name",
    }),
});
