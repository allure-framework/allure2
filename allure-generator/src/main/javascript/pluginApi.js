import pluginsRegistry from './util/pluginsRegistry';
import TreeLayout from './layouts/tree/TreeLayout';
import AppLayout from './layouts/application/AppLayout';
import WidgetStatusView from './components/widget-status/WidgetStatusView';

window.allure = {
    api: pluginsRegistry,
    components: {
        AppLayout: AppLayout,
        TreeLayout: TreeLayout,
        WidgetStatusView: WidgetStatusView
    }
};
