import {addTranslation} from './translation';
import router from '../router';
import {showView, notFound} from '../app';
import translate from '../helpers/t.js';
import WidgetsModel from '../data/widgets/WidgetsModel';


class AllurePluginsRegistry {
    tabs = [];
    testResultTabs = [];

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

    addWidget(tabName, widgetName, widget, model = WidgetsModel) {
        if (!this.widgets[tabName]) {
            this.widgets[tabName] = {};
        }
        this.widgets[tabName][widgetName] = {widget, model};
    }

    addTranslation(lang, json) {
        addTranslation(lang, json);
    }

    translate(name, options) {
        return translate(name, options);
    }

    addTestResultBlock(view, {position}) {
        this.testResultBlocks[position].push(view);
    }

    addTestResultTab(id, name, View) {
        this.testResultTabs.push({id, name, View});
    }
}

export default new AllurePluginsRegistry();
