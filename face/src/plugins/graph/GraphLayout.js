import AppLayout from '../../layouts/application/AppLayout';
import GraphCollection from './GraphCollection';
import GraphsView from './GraphsView';

export default class GraphLayout extends AppLayout {

    initialize() {
        super.initialize();
        this.testcases = new GraphCollection();
    }

    loadData() {
        return this.testcases.fetch();
    }

    getContentView() {
        return new GraphsView({collection: this.testcases});
    }
}
