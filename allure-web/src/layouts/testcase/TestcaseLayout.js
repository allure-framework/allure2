import PaneLayout from '../pane/PaneLayout';
import router from '../../router';

export default class ErrorLayout extends PaneLayout {
    loadData() {
        return Promise.resolve();
    }

    onStateChange() {
        const changed = Object.assign({}, this.state.changed);
        const paneView = this.getChildView('content');
        paneView.expanded = this.state.get('expanded');
        this.testcase.updatePanes('testcase', changed);
        paneView.updatePanesPositions();
    }

    onRouteUpdate(testcase, attachment) {
        const expanded = router.getUrlParams().expanded === 'true';
        this.state.set({testcase, attachment, expanded});
    }
}
