import pluginsRegistry from './util/pluginsRegistry';
import TreeLayout from './layouts/tree/TreeLayout';
import AppLayout from './layouts/application/AppLayout';
import WidgetStatusView from './components/widget-status/WidgetStatusView';
import {getSettingsForPlugin} from './util/settingsFactory';
import settings from './util/settings';

window.allure = {
    api: pluginsRegistry,
    getPluginSettings(name, defaults) {
        return getSettingsForPlugin(name, defaults);
    },
    settings: settings,
    components: {
        AppLayout: AppLayout,
        TreeLayout: TreeLayout,
        WidgetStatusView: WidgetStatusView
    }
};
