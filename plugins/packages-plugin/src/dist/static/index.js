'use strict';
allure.api.addTab('packages', {
    title: 'Packages', icon: 'fa fa-align-left',
    route: 'packages(/:testcaseId)',
    onEnter: (function () {
        var routeParams = Array.prototype.slice.call(arguments);
        return new allure.components.TreeLayout({
            routeParams: routeParams,
            tabName: 'Packages',
            baseUrl: 'packages',
            url: 'data/packages.json'
        });
    })
});
