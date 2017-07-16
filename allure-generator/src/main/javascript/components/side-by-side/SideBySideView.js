import './styles.css';
import {View} from 'backbone.marionette';
import {className, regions} from '../../decorators';
import template from './SideBySideView.hbs';
import TestResultView from '../testresult/TestResultView';
import TestResultModel from '../../data/testresult/TestResultModel';
import TreeCollection from '../../data/tree/TreeCollection';
import TreeView from '../tree/TreeView';

@className('side-by-side')
@regions({
    left: '.panel-left__content',
    right: '.panel-right__content'
})
class SideBySideView extends View {
    template = template;

    onAttach() {
        this.$('.panel-left').resizable({
            handles: 'e',
            maxWidth: 600
        });
    }

    onRender() {
        const url = 'data/suites.json';
        const collection = new TreeCollection([], {url});
        collection.fetch().then(() => {
            this.showChildView('left', new TreeView({collection}));

            const uid = collection.allResults[0].uid;
            const model = new TestResultModel({uid});
            model.fetch().then(() => {
                this.showChildView('right', new TestResultView({model}));
            });
        });
    }

    templateContext() {
        return {
            cls: 'side-by-side'
        };
    }

}

export default SideBySideView;