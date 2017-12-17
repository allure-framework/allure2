import {View} from 'backbone.marionette';
import {className} from '../../decorators';
import template from './DurationView.hbs';

@className('pane__section')
class DurationView extends View {
    template = template;
}

export default DurationView;
