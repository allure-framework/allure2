import {View} from 'backbone.marionette';
import {className} from '../../decorators';
import template from './TestResultBlockDurationView.hbs';

@className('pane__section')
class TestResultBlockDurationView extends View {
    template = template;
}

export default TestResultBlockDurationView;
