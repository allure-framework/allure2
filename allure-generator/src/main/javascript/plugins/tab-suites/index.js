allure.api.addTab('suites', {
    title: 'tab.suites.name',
    icon: 'fa fa-briefcase',
    route: 'suites(/)(*params)',
    onEnter: (params) => new allure.components.TreeLayout({
        params: params,
        tabName: 'tab.suites.name',
        baseUrl: 'suites',
        url: 'data/suites.json'
    })
});
