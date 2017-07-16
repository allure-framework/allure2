import {Model} from 'backbone';
import {View} from 'backbone.marionette';
import {className, regions} from '../../decorators/index';
import PaneSetView from '../pane-set/PaneSetView';
import TreeView from '../tree/TreeView';
import ErrorSplashView from '../error-splash/ErrorSplashView';

@className('tree-view-panes')
@regions({
    content: '.tree-view-panes__content'
})
class TreeViewPanes extends View {
    template = () => '<div class="tree-view-panes__content"></div>';

    initialize(options) {
        super.initialize(options);

        this.panes = new PaneSetView();
        this.treeState = this.options.treeState || new Model();
        this.listenTo(this.treeState, 'change:treeNode', this.showLeaf);
    }

    showLeaf() {
        const {tree, leafModel, leafView, baseUrl} = this.options;
        const treeNode = this.treeState.get('treeNode');
        const found = tree.findLeaf(treeNode.testGroup, treeNode.testResult);
        console.log(found)
        if (found) {
            const leaf = new leafModel({
                uid: treeNode.testResult
            });
            leaf.fetch().then(() => {
                this.panes.addPane('leaf', new leafView({
                    model: leaf,
                    baseUrl: `${baseUrl}/${treeNode.testGroup}/${treeNode.testResult}`
                }));
                this.panes.updatePanesPositions();
            });
        } else {
            this.panes.updatePanesPositions();
        }
    }

    onRender() {
        const {testGroup, testResult, tree, treeSorters, tabName, baseUrl} = this.options;
        this.showChildView('content', this.panes);
        this.panes.addPane('tree', new TreeView({
            collection: tree,
            treeState: this.treeState,
            treeSorters: treeSorters,
            tabName: tabName,
            baseUrl: baseUrl
        }));
        if (testGroup || testResult) {
            this.treeState.set('treeNode', {testGroup, testResult});
        } else {
            this.panes.updatePanesPositions();
        }
    }
}

export default TreeViewPanes;