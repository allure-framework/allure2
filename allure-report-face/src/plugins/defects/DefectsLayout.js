import {findWhere} from 'underscore';
import {Model} from 'backbone';
import PaneLayout from '../../layouts/pane/PaneLayout';
import router from '../../router';
import DefectsCollection from './defects-collection/DefectsCollection';
import DefectsListView from './defects-list/DefectsListView';
import DefectView from './defect-view/DefectView';

export default class DefectsLayoutView extends PaneLayout {

    initialize() {
        super.initialize();
        this.defects = new DefectsCollection();
    }

    loadData() {
        return this.defects.fetch();
    }

    buildDefectView(uid) {
        const defect = findWhere(this.defects.allDefects, {uid});
        if(!defect) {
            throw new Error(`Unable to find defect ${uid}`);
        }
        const model = new Model(defect);
        return new DefectView({model, state: this.state, baseUrl: 'defects'});
    }

    onStateChange() {
        const state = this.state;
        const changed = Object.assign({}, this.state.changed);
        const paneView = this.content.currentView;
        paneView.expanded = this.state.get('expanded');
        if(!paneView.getRegion('defects')) {
            paneView.addPane('defects', new DefectsListView({
                baseUrl: 'defects',
                collection: this.defects,
                state
            }));
        }
        paneView.updatePane('defect', changed, () => this.buildDefectView(changed.defect));
        this.testcase.updatePanes('defects/' + this.state.get('defect'), changed);
        paneView.updatePanesPositions();
    }

    onRouteUpdate(defect, testcase, attachment) {
        const expanded = router.getUrlParams().expanded === 'true';
        this.state.set({defect, testcase, attachment, expanded});
    }
}
