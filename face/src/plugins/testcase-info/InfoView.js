import {View} from 'backbone.marionette';
import {className} from '../../decorators';
import template from './InfoView.hbs';

@className('pane__section')
class InfoView extends View {
    template = template;

    serializeData() {
        return {
            time: this.model.get('time')
        };
    }
}

export default InfoView;
