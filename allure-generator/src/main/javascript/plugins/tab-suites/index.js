allure.api.addTab('suites', {
    title: 'tab.suites.name',
    icon: 'fa fa-briefcase',
    route: 'suites(/:testresult)',
    onEnter: (...routeParams) => new allure.components.TreeLayout({
        routeParams: routeParams,
        tabName: 'tab.suites.name',
        baseUrl: 'suites',
        url: 'data/suites.json'
    })
});
