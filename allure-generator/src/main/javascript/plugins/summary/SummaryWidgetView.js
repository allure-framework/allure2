import './styles.css';
import {View} from 'backbone.marionette';
import template from './SummaryWidgetView.hbs';
import {regions} from '../../decorators';
import StatusChart from '../graph/charts/StatusChart';

@regions({
    chart: '.summary-widget__chart'
})
class SummaryWidgetView extends View {
    template = template;

    onRender() {
        this.showChildView('chart', new StatusChart({
            statistic: this.model.get('statistic'),
            showLegend: false
        }));
    }

    serializeData() {
        const testRuns = this.model.get('testRuns');
        const length = testRuns && testRuns.length;
        return Object.assign(super.serializeData(), {
            isAggregated: length > 1,
            testRunsCount: length
        });
    }
}

export default SummaryWidgetView;