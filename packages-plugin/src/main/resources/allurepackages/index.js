allure.api.addTab('packages', {
    title: 'Packages', icon: 'fa fa-list',
    route: 'packages(/:testcaseId)(/:attachmentId)',
    onEnter: (...routeParams) => new allure.components.TreeLayout({
        routeParams: routeParams,
        tabName: 'Packages',
        baseUrl: 'packages',
        url: 'data/packages.json'
    })
});
