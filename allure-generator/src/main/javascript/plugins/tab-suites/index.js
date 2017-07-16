allure.api.addTab('suites', {
    title: 'tab.suites.name',
    icon: 'fa fa-briefcase',
    route: 'suites(/)(:testGroup)(/)(:testResult)(/)',
    onEnter: (testGroup, testResult) => new allure.components.TreeLayout({
        testGroup: testGroup,
        testResult: testResult,
        tabName: 'tab.suites.name',
        baseUrl: 'suites',
        url: 'data/suites.json'
    })
});
