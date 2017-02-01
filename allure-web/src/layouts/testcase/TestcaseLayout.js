import AppLayout from '../application/AppLayout';
import TestcaseModel from '../../data/testcase/TestcaseModel';
import TestcaseView from '../../components/testcase/TestcaseView';

export default class ErrorLayout extends AppLayout {
    initialize({testcaseUid}) {
        super.initialize();
        this.model = new TestcaseModel({uid: testcaseUid});
    }

    getContentView() {
        return new TestcaseView({model: this.model});
    }

    loadData() {
        return this.model.fetch();
    }
}
