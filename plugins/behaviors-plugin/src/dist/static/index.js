'use strict';
allure.api.addTab('behaviors', {
    title: 'Behaviors', icon: 'fa fa-list',
    route: 'behaviors(/:testcaseId)',
    onEnter: (function () {
        var routeParams = Array.prototype.slice.call(arguments);
        return new allure.components.TreeLayout({
            routeParams: routeParams,
            tabName: 'Behaviors',
            baseUrl: 'behaviors',
            url: 'data/behaviors.json'
        });
    })
});

allure.api.addWidget('behaviors', allure.components.WidgetStatusView.extend({
    title: 'Features by stories',
    baseUrl: 'behaviors',
    showLinks: false,
}));