import {ItemView} from 'backbone.marionette';
import {className} from '../../decorators';
import template from './DescriptionView.hbs';

@className('pane__section')
class DescriptionView extends ItemView {
    template = template;

    serializeData() {
        return this.model.get('description');
    }
}

export default DescriptionView;
