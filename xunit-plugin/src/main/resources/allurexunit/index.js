allure.api.addTab('xUnit', {
    title: 'xUnit',
    icon: 'fa fa-briefcase',
    route: 'xUnit(/:testcaseId)(/:attachmentId)',
    onEnter: (...routeParams) => new allure.components.TreeLayout({
        routeParams: routeParams,
        tabName: 'xUnit',
        baseUrl: 'xUnit',
        url: 'data/xunit.json'
    })
});
allure.api.addWidget('xunit', allure.components.WidgetStatusView.extend({
    title: 'xunit.widgetTitle',
    baseUrl: 'xUnit',
    showAllText: 'Show all test suites'
}));