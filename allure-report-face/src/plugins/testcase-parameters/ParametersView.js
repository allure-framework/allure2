import './styles.css';
import {ItemView} from 'backbone.marionette';
import {className} from '../../decorators';
import template from './ParametersView.hbs';

@className('pane__section')
class ParametersView extends ItemView {
    template = template;

    serializeData() {
        const parameters = this.model.get('parameters');
        return {
            arguments: parameters.filter(p => p.kind === 'ARGUMENT'),
            environment: parameters.filter(p => p.kind === 'ENVIRONMENT_VARIABLE')
        };
    }
}

export default ParametersView;
