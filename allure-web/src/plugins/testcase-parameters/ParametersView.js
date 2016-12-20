import './styles.css';
import {View} from 'backbone.marionette';
import {className} from '../../decorators';
import template from './ParametersView.hbs';

@className('pane__section')
class ParametersView extends View {
    template = template;

    serializeData() {
        return {
            parameters: this.model.get('parameters')
        };
    }
}

export default ParametersView;
