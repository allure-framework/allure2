import AppLayout from '../application/AppLayout';
import TreeViewPanes from '../../components/tree-panes/TreeViewPanes';
import TestResultModel from '../../data/testresult/TestResultModel';
import TestResultView from '../../components/testresult/TestResultView';
import TreeCollection from '../../data/tree/TreeCollection';
import {Model} from 'backbone';
import SideBySideView from '../../components/side-by-side/SideBySideView';

export default class TreeLayout extends AppLayout {

    initialize({url}) {
        super.initialize();
        this.tree = new TreeCollection([], {url});
        this.treeState = new Model();
    }

    loadData() {
        return this.tree.fetch();
    }

    getContentView() {
        // const {testGroup, testResult, baseUrl} = this.options;
        // const treeSorters = [];
        // const tabName = 'Suites';
        // const tree = this.tree;
        // const leafModel = TestResultModel;
        // const leafView = TestResultView;
        // const treeState = this.treeState;

        // return new TreeViewPanes({
        //     treeState,
        //     testGroup,
        //     testResult,
        //     tree,
        //     treeSorters,
        //     tabName,
        //     baseUrl,
        //     leafModel,
        //     leafView
        // });
        //
        return new SideBySideView();
    }

    onRouteUpdate(testGroup, testResult) {
        this.treeState.set('treeNode', {testGroup, testResult});
    }
}
