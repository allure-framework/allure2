allure.api.addTab("categories", {
  title: "tab.categories.name",
  icon: "fa fa-flag",
  route: "categories(/)(:testGroup)(/)(:testResult)(/)(:testResultTab)(/)",
  onEnter: (testGroup, testResult, testResultTab) =>
    new allure.components.TreeLayout({
      testGroup,
      testResult,
      testResultTab,
      tabName: "tab.categories.name",
      baseUrl: "categories",
      url: "data/categories.json",
      csvUrl: "data/categories.csv",
    }),
});
