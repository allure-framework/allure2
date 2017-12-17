import {CollectionView} from 'backbone.marionette';
import {className} from '../../decorators';
import pluginsRegistry from '../../utils/pluginsRegistry';
import {Collection, Model} from 'backbone';

@className('test-result-overview')
class TestResultOverviewView extends CollectionView {

    initialize(options) {
        super.initialize(options);

        const models = pluginsRegistry.testResultBlocks.map(data => new Model(data));
        this.collection = new Collection(models, {comparator: 'order'});
    }

    childView(model) {
        return model.get('view');
    }

    childViewOptions() {
        return {
            model: this.options.model
        };
    }

    filter(child) {
        const condition = child.get('condition') || (() => true);
        return condition(this.options.model);
    }
}

export default TestResultOverviewView;