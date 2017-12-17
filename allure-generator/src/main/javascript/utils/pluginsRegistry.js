import {addTranslation} from './translation';
import router from '../router';
import {notFound, showView} from '../app';
import translate from '../helpers/t.js';
import WidgetsModel from '../data/widgets/WidgetsModel';

const positions = {
    'before': 30,
    'after': 150,
    'tag': 10
};

class AllurePluginsRegistry {
    tabs = [];
    testResultTabs = [];
    testResultBlocks = [];

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
        translate(name, options);
    }

    addTestResultBlock(view, {position, order = 100, condition = () => true}) {
        this.testResultBlocks.push({
            view,
            order: position ? positions[position] : order,
            condition
        });
    }

    addTestResultTab(id, name, View) {
        this.testResultTabs.push({id, name, View});
    }
}

export default new AllurePluginsRegistry();
