import './styles.scss';
import {View} from 'backbone.marionette';
import {className, on} from '../../decorators';
import template from './TestResultBlockParametersView.hbs';

@className('pane__section')
class TestResultBlockParametersView extends View {
    template = template;

    serializeData() {
        return {
            parameters: this.model.get('parameters')
        };
    }

    @on('click .environment')
    onParameterClick() {
        this.$('.environment').toggleClass('line-ellipsis', false);
    }
}

export default TestResultBlockParametersView;
