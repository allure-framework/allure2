allure.api.addTab('categories', {
    title: 'Categories', icon: 'fa fa-flag',
    route: 'categories(/:testcaseId)',
    onEnter: (function () {
        const routeParams = Array.prototype.slice.call(arguments);
        return new allure.components.TreeLayout({
            routeParams: routeParams,
            tabName: 'Categories',
            baseUrl: 'categories',
            url: 'data/categories.json'
        });
    })
});