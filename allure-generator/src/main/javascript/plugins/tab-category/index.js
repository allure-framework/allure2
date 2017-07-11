allure.api.addTab('categories', {
    title: 'tab.categories.name', icon: 'fa fa-flag',
    route: 'categories(/:testResultUid)',
    onEnter: (function () {
        const routeParams = Array.prototype.slice.call(arguments);
        return new allure.components.TreeLayout({
            routeParams: routeParams,
            tabName: 'tab.categories.name',
            baseUrl: 'categories',
            url: 'data/categories.json'
        });
    })
});