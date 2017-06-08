import pluginsRegistry from './util/pluginsRegistry';
import TreeLayout from './layouts/tree/TreeLayout';
import AppLayout from './layouts/application/AppLayout';
import WidgetStatusView from './components/widget-status/WidgetStatusView';
import createSettingsModel from './data/settings/createSettingsModel';

window.allure = {
    api: pluginsRegistry,
    getPluginSettings(name, defaults) {
        const SettingsModel = createSettingsModel(name, defaults);
        const settings = new SettingsModel();
        settings.fetch();
        return settings;
    },
    components: {
        AppLayout: AppLayout,
        TreeLayout: TreeLayout,
        WidgetStatusView: WidgetStatusView
    }
};
