import './styles.scss';
import {View} from 'backbone.marionette';
import template from './SummaryWidgetView.hbs';
import {regions} from '../../decorators';
import PieChartView from '../../components/graph-pie-chart/PieChartView';


@regions({
    chart: '.summary-widget__chart'
})
class SummaryWidgetView extends View {
    template = template;

    onRender() {
        this.showChildView('chart', new PieChartView({
            model: this.model,
            showLegend: false
        }));
    }

    serializeData() {
        const testRuns = this.model.get('testRuns');
        const length = testRuns && testRuns.length;
        return Object.assign(super.serializeData(), {
            isAggregated: length > 1,
            launchesCount: length
        });
    }
}

export default SummaryWidgetView;