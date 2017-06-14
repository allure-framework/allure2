'use strict';

allure.api.addTranslation('en', {
    tab: {
        behaviors: {
            name: 'Behaviors'
        }
    },
    widget: {
        behaviors: {
            name: 'Features by stories',
            showAll: 'show all'
        }
    }
});

allure.api.addTranslation('ru', {
    tab: {
        behaviors: {
            name: 'Функциональность'
        }
    },
    widget: {
        behaviors: {
            name: 'Функциональность',
            showAll: 'показать все'
        }
    }
});

allure.api.addTranslation('zh', {
    tab: {
        behaviors: {
            name: '功能'
        }
    },
    widget: {
        behaviors: {
            name: '特性场景',
            showAll: '显示所有'
        }
    }
});

allure.api.addTranslation('de', {
    tab: {
        behaviors: {
            name: 'Verhalten'
        }
    },
    widget: {
        behaviors: {
            name: 'Features nach Stories',
            showAll: 'Zeige alle'
        }
    }
});

allure.api.addTab('behaviors', {
    title: 'tab.behaviors.name', icon: 'fa fa-list',
    route: 'behaviors(/:testcaseId)',
    onEnter: (function () {
        var routeParams = Array.prototype.slice.call(arguments);
        return new allure.components.TreeLayout({
            routeParams: routeParams,
            tabName: 'tab.behaviors.name',
            baseUrl: 'behaviors',
            url: 'data/behaviors.json'
        });
    })
});

allure.api.addWidget('behaviors', allure.components.WidgetStatusView.extend({
    title: 'widget.behaviors.name',
    baseUrl: 'behaviors',
    showLinks: false,
    showAllText: 'widget.behaviors.showAll'
}));