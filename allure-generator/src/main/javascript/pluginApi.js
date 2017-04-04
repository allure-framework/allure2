import pluginsRegistry from './util/pluginsRegistry';
import TreeLayout from './layouts/tree/TreeLayout';
import WidgetStatusView from './components/widget-status/WidgetStatusView';

window.allure = {
    api: pluginsRegistry,
    components: {
        TreeLayout: TreeLayout,
        WidgetStatusView: WidgetStatusView
    }
};
