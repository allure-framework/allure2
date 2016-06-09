import PaneLayout from '../../layouts/pane/PaneLayout';
import XUnitCollection from './xunit-collection/XUnitCollection';
import router from '../../router';
import TestsuitesListView from './testsuites-list/TestsuitesListView';

export default class XUnitLayout extends PaneLayout {

    initialize() {
        super.initialize();
        this.suites = new XUnitCollection();
    }

    loadData() {
        return this.suites.fetch();
    }

    onStateChange() {
        const state = this.state;
        const changed = Object.assign({}, this.state.changed);
        const paneView = this.content.currentView;
        paneView.expanded = this.state.get('expanded');
        if(!paneView.getRegion('testrun')) {
            paneView.addPane('testrun', new TestsuitesListView({
                collection: this.suites,
                state
            }));
        }
        this.testcase.updatePanes('xUnit', changed);
        paneView.updatePanesPositions();
    }

    onRouteUpdate(testcase, attachment) {
        const expanded = router.getUrlParams().expanded === 'true';
        this.state.set({testcase, attachment, expanded});
    }
}
