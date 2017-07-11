import './styles.css';
import {View} from 'backbone.marionette';
import {className} from '../../decorators';
import template from './BackPanelView.hbs';

@className('back-panel')
class TestResultBackPanelView extends View {
    template = template;

    templateContext() {
        const {url} = this.options;
        return {
            text: url.split('/')[0],
            url
        };
    }
}

export default TestResultBackPanelView;
