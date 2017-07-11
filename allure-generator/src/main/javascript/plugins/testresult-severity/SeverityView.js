import {View} from 'backbone.marionette';
import {className} from '../../decorators/index';
import {escapeExpression} from 'handlebars/runtime';
import translate from '../../helpers/t';

@className('pane__section')
class SeverityView extends View {
    template(data) {
        return data.severity ? `${translate('testResult.severity.name')}: ${escapeExpression(data.severity)}` : '';
    }

    serializeData() {
        const extra = this.model.get('extra');
        return {
            severity: extra ? extra.severity : null
        };
    }
}

export default SeverityView;