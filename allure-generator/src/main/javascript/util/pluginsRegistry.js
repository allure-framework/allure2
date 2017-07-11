import {addTranslation} from './translation';
import router from '../router';
import {showView, notFound} from '../app';
import translate from '../helpers/t.js';

class AllurePluginsRegistry {
    tabs = [];

    testResultBlocks = {
        tag: [],
        before: [],
        after: []
    };

    widgets = {};

    addTab(tabName, {title, icon, route, onEnter = notFound} = {}) {
        title = title || tabName;
        this.tabs.push({tabName, title, icon});
        router.route(route, tabName);
        router.on('route:' + tabName, showView(onEnter));
    }

    addWidget(tabName, widgetName, Widget) {
        if (!this.widgets[tabName]) {
            this.widgets[tabName] = {};
        }
        this.widgets[tabName][widgetName] = Widget;
    }

    addTranslation(lang, json) {
        addTranslation(lang, json);
    }

    translate(name, options) {
        translate(name, options);
    }

    addTestResultBlock(view, {position}) {
        this.testResultBlocks[position].push(view);
    }
}

export default new AllurePluginsRegistry();
