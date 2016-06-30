import pluginApi from './pluginApi';
import TreeLayout from './components/tree/TreeLayout';
import WidgetStatusView from './components/widget-status/WidgetStatusView';

//TODO solve the problems with treeLayout and move it to pluginApi
window.allure = {
    api: pluginApi,
    components: {
        TreeLayout: TreeLayout,
        WidgetStatusView: WidgetStatusView
    }
};