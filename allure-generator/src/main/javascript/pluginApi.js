import pluginsRegistry from './utils/pluginsRegistry';
import TreeLayout from './layouts/tree/TreeLayout';
import AppLayout from './layouts/application/AppLayout';
import WidgetStatusView from './components/widget-status/WidgetStatusView';
import TrendChartView from './components/graph-trend-chart/TrendChartView';
import TrendCollection from './data/trend/TrendCollection.js';
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
        WidgetStatusView: WidgetStatusView,
        TrendChartView: TrendChartView
    },
    collections: {
        TrendCollection: TrendCollection
    }
};
