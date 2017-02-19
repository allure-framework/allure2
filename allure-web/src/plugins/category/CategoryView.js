import {View} from 'backbone.marionette';
import template from './CategoryView.hbs';
import {className} from '../../decorators/index';

@className('pane__section')
class SeverityView extends View {
    template = template;

    serializeData() {
        const extra = this.model.get('extra');
        return {
            categories: extra ? extra.categories : null
        };
    }
}

export default SeverityView;