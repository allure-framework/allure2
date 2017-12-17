import {View} from 'backbone.marionette';
import {className} from '../../decorators';
import template from './TestResultBlockTagsView.hbs';

@className('pane__section')
class TestResultBlockTagsView extends View {
    template = template;

    serializeData() {
        const extra = this.model.get('extra');
        return {
            tags: extra ? extra.tags : null
        };
    }
}

export default TestResultBlockTagsView;
