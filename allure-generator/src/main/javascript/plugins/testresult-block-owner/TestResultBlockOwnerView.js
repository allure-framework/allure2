import {View} from 'backbone.marionette';
import {className} from '../../decorators/index';
import translate from '../../helpers/t';
import {escapeExpression} from 'handlebars/runtime';

@className('pane__section')
class TestResultBlockOwnerView extends View {
    template = (data) => `<span class="fa fa-user"></span> ${translate('testResult.owner.name')}: ${escapeExpression(data.owner)}`;

    serializeData() {
        const extra = this.model.get('extra');
        return {
            owner: extra ? extra.owner : null
        };
    }
}

export default TestResultBlockOwnerView;