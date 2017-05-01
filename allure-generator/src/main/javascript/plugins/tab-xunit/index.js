allure.api.addTab('xUnit', {
    title: 'tab.xunit.name',
    icon: 'fa fa-briefcase',
    route: 'xUnit(/:testcaseId)',
    onEnter: (...routeParams) => new allure.components.TreeLayout({
        routeParams: routeParams,
        tabName: 'tab.xunit.name',
        baseUrl: 'xUnit',
        url: 'data/xunit.json'
    })
});
// allure.api.addWidget('xunit', allure.components.WidgetStatusView.extend({
//     title: 'xunit',
//     baseUrl: 'xUnit',
//     showAllText: 'Show all test suites'
// }));
