'use strict';

allure.api.addTranslation('en', {
    tab: {
        packages: {
            name: 'Packages'
        }
    }
});

allure.api.addTranslation('ru', {
    tab: {
        packages: {
            name: 'Пакеты'
        }
    }
});

allure.api.addTab('packages', {
    title: 'tab.packages.name', icon: 'fa fa-align-left',
    route: 'packages(/:testcaseId)',
    onEnter: (function () {
        var routeParams = Array.prototype.slice.call(arguments);
        return new allure.components.TreeLayout({
            routeParams: routeParams,
            tabName: 'tab.packages.name',
            baseUrl: 'packages',
            url: 'data/packages.json'
        });
    })
});
