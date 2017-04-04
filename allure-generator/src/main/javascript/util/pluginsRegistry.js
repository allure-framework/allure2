import {addTranslation} from './translation';
import router from '../router';
import {showView, notFound} from '../app';

class AllurePluginsRegistry {
    tabs = [];

    testcaseBlocks = {
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

    addWidget(name, Widget) {
        this.widgets[name] = Widget;
    }

    addTranslation(lang, json) {
        addTranslation(lang, json);
    }

    addTestcaseBlock(view, {position}) {
        this.testcaseBlocks[position].push(view);
    }
}

export default new AllurePluginsRegistry();
