import PaneLayout from '../pane/PaneLayout';
import BackPanelView from '../../components/back-panel/BackPanelView';
import router from '../../router';

export default class TestcaseLayout extends PaneLayout {
    loadData() {
        return Promise.resolve();
    }

    onStateChange() {
        const changed = Object.assign({}, this.state.changed);
        const paneView = this.getChildView('content');
        if(router.previousUrl && !(router.previousUrl.indexOf('testcase') === 0) && !paneView.getRegion('back')) {
            paneView.addPane('back', new BackPanelView({
                url: router.previousUrl
            }));
        }
        paneView.expanded = this.state.get('expanded') || !this.state.get('attachment');
        this.testcase.updatePanes('testcase', changed);
        paneView.updatePanesPositions();
    }

    onRouteUpdate(testcase, attachment) {
        const expanded = router.getUrlParams().expanded === 'true';
        this.state.set({testcase, attachment, expanded});
    }
}
