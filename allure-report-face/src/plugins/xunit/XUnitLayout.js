import PaneLayout from '../../layouts/pane/PaneLayout';
import XUnitCollection from './xunit-collection/XUnitCollection';
import router from '../../router';
import TestsuitesListView from './testsuites-list/TestsuitesListView';
import TestsuiteView from './testsuite-view/TestsuiteView';

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
        paneView.updatePane('testsuite', changed, () => new TestsuiteView({
            testsuite: this.suites.findWhere({uid: changed.testsuite}),
            baseUrl: 'xUnit',
            state
        }));
        this.testcase.updatePanes('xUnit/' + this.state.get('testsuite'), changed);
        paneView.updatePanesPositions();
    }

    onRouteUpdate(testsuite, testcase, attachment) {
        const expanded = router.getUrlParams().expanded === 'true';
        this.state.set({testsuite, testcase, attachment, expanded});
    }
}
