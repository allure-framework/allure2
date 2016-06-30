import PaneLayout from '../../layouts/pane/PaneLayout';
import TreeCollection from './TreeCollection';
import TreeView from './TreeView';
import router from '../../router';

export default class TreeLayout extends PaneLayout {

    initialize({url, baseUrl, tabName}) {
        super.initialize();
        const Collection = TreeCollection.extend({url: url});
        this.items = new Collection;
        this.baseUrl = baseUrl;
        this.tabName = tabName;
    }

    loadData() {
        return this.items.fetch();
    }

    onStateChange() {
        const changed = Object.assign({}, this.state.changed);
        const paneView = this.content.currentView;
        paneView.expanded = this.state.get('expanded');
        if (!paneView.getRegion('testrun')) {
            paneView.addPane('testrun', new TreeView({
                collection: this.items,
                state: this.state,
                tabName: this.tabName,
                baseUrl: this.baseUrl
            }));
        }
        this.testcase.updatePanes(this.baseUrl, changed);
        paneView.updatePanesPositions();
    }

    onRouteUpdate(testcase, attachment) {
        const expanded = router.getUrlParams().expanded === 'true';
        this.state.set({testcase, attachment, expanded});
    }
}
