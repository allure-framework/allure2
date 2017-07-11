import PaneLayout from '../pane/PaneLayout';
import TreeCollection from '../../data/tree/TreeCollection';
import TreeView from '../../components/tree/TreeView';
import router from '../../router';

export default class TreeLayout extends PaneLayout {

    initialize({url}) {
        super.initialize();
        this.items = new TreeCollection([], {url});
    }

    loadData() {
        return this.items.fetch();
    }

    onStateChange() {
        const changed = Object.assign({}, this.state.changed);
        const paneView = this.getChildView('content');
        paneView.expanded = this.state.get('expanded');
        if (!paneView.getRegion('testrun')) {
            paneView.addPane('testrun', new TreeView({
                collection: this.items,
                state: this.state,
                tabName: this.options.tabName,
                baseUrl: this.options.baseUrl
            }));
        }
        this.testResult.updatePanes(this.options.baseUrl, changed);
        paneView.updatePanesPositions();
    }

    onRouteUpdate(testResult, attachment) {
        const expanded = router.getUrlParams().expanded === 'true';
        this.state.set({testResult, attachment, expanded});
    }
}
