import './styles.scss';
import {View} from 'backbone.marionette';
import {className} from '../../decorators';
import template from './TestResultBlockLinksView.hbs';

@className('pane__section')
class TestResultBlockLinksView extends View {
    template = template;

    serializeData() {
        return {
            links: this.model.get('links')
        };
    }
}

export default TestResultBlockLinksView;
