import './styles.css';
import {LayoutView} from 'backbone.marionette';
import $ from 'jquery';
import {className} from '../../decorators';
import DurationChart from './charts/DurationChart';
import StatusChart from './charts/StatusChart';
import SeverityChart from './charts/SeverityChart';


@className('charts-grid')
class GraphsView extends LayoutView {
    template() { return ''; }

    onShow() {
        const collection = this.collection;
        this.addChart('Status', new StatusChart({
            statistic: this.getStatusChartData(),
            showLegend: true
         }));
        this.addChart('Severity', new SeverityChart({collection}));
        this.addChart('Duration', new DurationChart({collection}));
    }

    addChart(name, chart) {
        const container = $(`<div class="chart__wrap">
            <div class="chart island">
                <h2 class="chart__title">${name}</h2>
                <div class="chart__body"></div>
            </div>
        </div>`);
        this.$el.append(container);
        this.addRegion(name, {el: container.find('.chart__body')});
        this.getRegion(name).show(chart);
    }

    getStatusChartData() {
        return this.collection.reduce((stats, testcase) => {
            stats[testcase.get('status').toLowerCase()]++;
            return stats;
        }, {
            total: this.collection.length,
            failed: 0,
            broken: 0,
            canceled: 0,
            pending: 0,
            passed: 0
        });
    }
}

export default GraphsView;
