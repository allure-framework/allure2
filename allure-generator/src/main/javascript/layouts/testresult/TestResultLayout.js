import AppLayout from '../application/AppLayout';
import TestResultView from '../../components/testresult/TestResultView';
import {Model} from 'backbone';
import TestResultModel from '../../data/testresult/TestResultModel';

export default class TestResultLayout extends AppLayout {

    initialize({uid}) {
        super.initialize();
        this.uid = uid;
        this.model = new TestResultModel({uid});
        this.routeState = new Model();
    }

    loadData() {
        return this.model.fetch();
    }

    getContentView() {
        const baseUrl = `#testresult/${this.uid}`;
        return new TestResultView({baseUrl, model: this.model, routeState: this.routeState});
    }

    onViewReady() {
        const {uid, tabName} = this.options;
        this.onRouteUpdate(uid, tabName);
    }

    onRouteUpdate(uid, tabName) {
        this.routeState.set('testResultTab', tabName);
    }

    shouldKeepState(uid) {
        return this.uid === uid;
    }
}
