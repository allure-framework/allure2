allure.api.addTab('behaviors', {
    title: 'Behaviors', icon: 'fa fa-list',
    route: 'behaviors(/:testcaseId)(/:attachmentId)',
    onEnter: (...routeParams) => new allure.components.TreeLayout({
        routeParams: routeParams,
        tabName: 'Behaviors',
        baseUrl: 'behaviors',
        url: 'data/behaviors.json'
    })
});
