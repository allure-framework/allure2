import template from './StatusWidgetView.hbs';
import {View} from 'backbone.marionette';
import {className, regions} from '../../decorators';
import {Model} from 'backbone';
import PieChartView from '../../components/graph-pie-chart/PieChartView';

@className('status-widget')
@regions({
    chart: '.status-widget__content'
})
class StatusWidgetView extends View {
    template = template;

    onRender() {
        this.showChildView('chart', new PieChartView({
            model: this.getStatusChartData(),
            showLegend: true
        }));
    }

    getStatusChartData() {
        this.items = this.model.get('items');
        const statistic =  this.items.reduce(
            (stats, testResult) => {
                stats[testResult.status.toLowerCase()]++;
                return stats;
            }, {
            total: this.items.length,
            failed: 0,
            broken: 0,
            skipped: 0,
            passed: 0,
            unknown: 0
        });
        return new Model({statistic});
    }
}

export default StatusWidgetView;
