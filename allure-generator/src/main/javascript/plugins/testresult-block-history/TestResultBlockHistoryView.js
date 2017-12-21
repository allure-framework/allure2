import {View} from 'backbone.marionette';
import {className} from '../../decorators/index';
import translate from '../../helpers/t';
import {escapeExpression} from 'handlebars/runtime';

function formatNumber(number) {
    return (Math.floor(number * 100) / 100).toString();
}

function getSuccessRate(history) {
    if (!history || !history.statistic || !history.statistic.total) {
        return 'unknown';
    }
    const {passed, total} = history.statistic;
    return formatNumber((passed || 0) / total * 100) + '%';
}

@className('pane__section')
class TestResultBlockHistoryView extends View {
    template = (data) => `<span class="fa fa-percent"></span> ${translate('testResult.history.successRate')}: ${escapeExpression(data.successRate)}`;

    serializeData() {
        const extra = this.model.get('extra');
        const history = extra ? extra.history : null;
        return {
            statistic: history ? history.statistic : null,
            successRate: getSuccessRate(history)
        };
    }
}

export default TestResultBlockHistoryView;