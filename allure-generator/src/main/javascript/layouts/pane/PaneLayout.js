import {Model} from 'backbone';
import AppLayout from '../../layouts/application/AppLayout';
import PaneSetView from '../../components/pane-set/PaneSetView';
import TestResultPanes from '../../util/TestResultPanes';

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
        this.testResult = new TestResultPanes(this.state, this.getChildView('content'));
        this.onRouteUpdate(...this.options.routeParams);
    }

    onStateChange() {}

    onRouteUpdate() {}
}
