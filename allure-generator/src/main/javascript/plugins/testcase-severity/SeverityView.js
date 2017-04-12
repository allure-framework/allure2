import {View} from 'backbone.marionette';
import {className} from '../../decorators/index';
import {escapeExpression} from 'handlebars/runtime';

@className('pane__section')
class SeverityView extends View {
    template(data) {
        return data.severity ? `Severity: ${escapeExpression(data.severity)}` : '';
    }

    serializeData() {
        const extra = this.model.get('extra');
        return {
            severity: extra ? extra.severity : null
        };
    }
}

export default SeverityView;