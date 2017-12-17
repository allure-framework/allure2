import {View} from 'backbone.marionette';
import {className} from '../../decorators';
import template from './TestResultBlockDescriptionView.hbs';

@className('pane__section')
class TestResultBlockDescriptionView extends View {
    template = template;

    serializeData() {
        return {
            descriptionHtml: this.model.get('descriptionHtml')
        };
    }
}

export default TestResultBlockDescriptionView;
