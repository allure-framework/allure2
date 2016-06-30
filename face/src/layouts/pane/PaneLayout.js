import {Model} from 'backbone';
import AppLayout from '../../layouts/application/AppLayout';
import PaneSetView from '../../components/pane-set/PaneSetView';
import TestcasePanes from '../../util/TestcasePanes';

export default class PaneLayout extends AppLayout {

    initialize() {
        super.initialize();
        this.state = new Model();
        this.listenTo(this.state, 'change', this.onStateChange, this);
    }

    getContentView() {
        return new PaneSetView();
    }

    onViewReady() {
        this.testcase = new TestcasePanes(this.state, this.content.currentView);
        this.onRouteUpdate(...this.options.routeParams);
    }

    onStateChange() {}

    onRouteUpdate() {}
}
