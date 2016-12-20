import PaneLayout from '../../layouts/pane/PaneLayout';
import router from '../../router';
import TimelineModel from './TimelineModel';
import TimelineView from './TimelineView';

export default class TimelineLayout extends PaneLayout {

    initialize() {
        super.initialize();
        this.model = new TimelineModel();
    }

    loadData() {
        return this.model.fetch();
    }

    onStateChange() {
        const changed = Object.assign({}, this.state.changed);
        const paneView = this.getChildView('content');
        paneView.expanded = this.state.get('expanded');
        if(!paneView.getRegion('timeline')) {
            paneView.addPane('timeline', new TimelineView({model: this.model}));
        } else {
            paneView.getChildView('timeline').onRender();
        }
        this.testcase.updatePanes('timeline', changed);
        paneView.updatePanesPositions();
    }

    onRouteUpdate(testcase, attachment) {
        const expanded = router.getUrlParams().expanded === 'true';
        this.state.set({testcase, attachment, expanded});
    }
}
