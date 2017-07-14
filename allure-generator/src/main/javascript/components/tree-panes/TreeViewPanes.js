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
        this.treeState = new Model();
        this.listenTo(this.treeState, 'change:id', this.showLeaf);
    }

    showLeaf() {
        const uid = this.treeState.get('uid');
        const {leafModel, leafView, baseUrl} = this.options;
        const leaf = new leafModel({uid});
        leaf.set({uid});
        leaf.fetch().then(() => {
            this.panes.addPane('leaf', new leafView({
                model: leaf,
                baseUrl: baseUrl + '/' + uid
            }));
            this.panes.updatePanesPositions();
        });
    }

    onRender() {
        const {path, tree, treeSorters, tabName, baseUrl} = this.options;
        this.showChildView('content', this.panes);
        this.panes.addPane('tree', new TreeView({
            collection: tree,
            treeState: this.treeState,
            treeSorters: treeSorters,
            tabName: tabName,
            baseUrl: baseUrl
        }));
        if (path.length > 0) {
            const uid = tree.findNode(path);
            console.log(uid);
            if (uid) {
                this.treeState.set('uid', uid);
            } else {
                this.panes.addPane('notFound', new ErrorSplashView({code: 404, message: `Path ${path} not found`}));
                this.panes.updatePanesPositions();
            }
        }
    }
}

export default TreeViewPanes;