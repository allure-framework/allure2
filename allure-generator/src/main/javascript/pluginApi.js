import pluginsRegistry from './utils/pluginsRegistry';
import TreeLayout from './layouts/tree/TreeLayout';
import AppLayout from './layouts/application/AppLayout';
import WidgetStatusView from './components/widget-status/WidgetStatusView';
import {getSettingsForPlugin} from './utils/settingsFactory';
import settings from './utils/settings';

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
