import PaneLayout from '../../layouts/pane/PaneLayout';
import BehaviorsModel from './data/BehaviorsModel';
import router from '../../router';
import BehaviorsTreeView from './behaviors-tree/BehaviorsTreeView';
import BehaviorView from './behavior-view/BehaviorView';
export default class BehaviorsLayout extends PaneLayout {

    initialize() {
        super.initialize();
        this.behaviors = new BehaviorsModel();
    }

    loadData() {
        return this.behaviors.fetch();
    }

    onStateChange() {
        const state = this.state;
        const changed = Object.assign({}, this.state.changed);
        const paneView = this.content.currentView;
        paneView.expanded = this.state.get('expanded');
        if(!paneView.getRegion('behaviors')) {
            paneView.addPane('behaviors', new BehaviorsTreeView({
                model: this.behaviors,
                state
            }));
        }
        paneView.updatePane('behavior', changed, () => new BehaviorView({
            model: this.behaviors.getBehavior(this.state.get('behavior')),
            state
        }));
        this.testcase.updatePanes('behaviors/' + this.state.get('behavior'), changed);
        paneView.updatePanesPositions();
    }

    onRouteUpdate(behavior, testcase, attachment) {
        const expanded = router.getUrlParams().expanded === 'true';
        this.state.set({behavior, testcase, attachment, expanded});
    }
}
