/* eslint-disable */
import TrendChartView from "./components/graph-trend-chart/TrendChartView";
import WidgetStatusView from "./components/widget-status/WidgetStatusView";
import TrendCollection from "./data/trend/TrendCollection.js";
import AppLayout from "./layouts/application/AppLayout";
import TreeLayout from "./layouts/tree/TreeLayout";
import pluginsRegistry from "./utils/pluginsRegistry";
import settings from "./utils/settings";
import { getSettingsForPlugin } from "./utils/settingsFactory";

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
    TrendChartView: TrendChartView,
  },
  collections: {
    TrendCollection: TrendCollection,
  },
};
