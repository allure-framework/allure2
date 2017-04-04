import {View} from 'backbone.marionette';
import template from './SeverityView.hbs';
import {className} from '../../decorators/index';

@className('pane__section')
class SeverityView extends View {
    template = template;

    serializeData() {
        var extra = this.model.get('extra');
        return {
            severity: extra ? extra.severity : null
        };
    }
}

export default SeverityView;