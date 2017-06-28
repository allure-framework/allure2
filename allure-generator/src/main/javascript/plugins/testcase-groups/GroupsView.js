import {View} from 'backbone.marionette';
import {className} from '../../decorators';
import template from './GroupsView.hbs';

@className('pane__section')
class DurationView extends View {
    template = template;

    serializeData() {
        return {
            groupLinks: this.model.get('groupLinks')
        };
    }
}

export default DurationView;
