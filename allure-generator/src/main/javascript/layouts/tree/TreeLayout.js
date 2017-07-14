import AppLayout from '../application/AppLayout';
import TreeViewPanes from '../../components/tree-panes/TreeViewPanes';
import TestResultModel from '../../data/testresult/TestResultModel';
import TestResultView from '../../components/testresult/TestResultView';
import TreeCollection from '../../data/tree/TreeCollection';

export default class TreeLayout extends AppLayout {

    initialize({url}) {
        super.initialize();
        this.tree = new TreeCollection([], {url});
    }

    loadData() {
        return this.tree.fetch();
    }

    getContentView() {
        const {params, baseUrl} = this.options;
        const path = params ? params.split('/') : [];
        const treeSorters = [];
        const tabName = 'Suites';
        const tree = this.tree;
        const leafModel = TestResultModel;
        const leafView = TestResultView;

        return new TreeViewPanes({
            path,
            tree,
            treeSorters,
            tabName,
            baseUrl,
            leafModel,
            leafView
        });
    }

    onRouteUpdate(testResult) {
        this.state.set({testResult});
    }
}
