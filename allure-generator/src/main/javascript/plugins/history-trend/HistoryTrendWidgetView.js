import './styles.css';
import template from './HistoryTrendWidgetView.hbs';
import {View} from 'backbone.marionette';
import {className, regions} from '../../decorators/index';
import TestTrendChart from '../graph/charts/TestTrendChart';

@regions({
    chart: '.history-trend__chart'
})
@className('history-trend')
class HistoryTrendWidgetView extends View {
    template = template;

    onRender() {
        this.showChildView('chart', new TestTrendChart({
            items: this.model.get('items')
        }));
    }
}

export default HistoryTrendWidgetView;
