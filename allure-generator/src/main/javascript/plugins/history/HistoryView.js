import './styles.css';
import {View} from 'backbone.marionette';
import template from './HistoryView.hbs';

class HistoryView extends View {
    template = template;

    serializeData() {
        var extra = this.model.get('extra');
        var history = extra ? extra.history : null;
        return {
            history: history,
            successRate: this.getSuccessRate(history)
        };
    }

    getSuccessRate(history) {
        if (!history || !history.statistic || !history.statistic.total) {
            return 'unknown';
        }
        const {passed, total} = history.statistic;
        return this.formatNumber((passed || 0) / total * 100) + '%';
    }

    formatNumber(number) {
        return (Math.floor(number * 100) / 100).toString();
    }
}

export default HistoryView;