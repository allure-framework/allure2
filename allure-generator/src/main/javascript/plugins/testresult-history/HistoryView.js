import './styles.scss';
import {View} from 'backbone.marionette';
import template from './HistoryView.hbs';
import {className} from '../../decorators';

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

@className('test-result-history')
class HistoryView extends View {
    template = template;

    serializeData() {
        const extra = this.model.get('extra');
        const history = extra ? extra.history : null;
        return {
            cls: this.className,
            history: history,
            successRate: getSuccessRate(history)
        };
    }

}

export default HistoryView;