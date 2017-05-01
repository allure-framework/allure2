import './styles.css';
import {View} from 'backbone.marionette';
import $ from 'jquery';
import {className} from '../../decorators';
import DurationChart from './charts/DurationChart';
import StatusChart from './charts/StatusChart';
import SeverityChart from './charts/SeverityChart';
import t from '../../helpers/t';

@className('charts-grid')
class GraphsView extends View {
    template() { return ''; }

    onAttach() {
        const collection = this.collection;
        this.addChart('chart.status.name', new StatusChart({
            statistic: this.getStatusChartData(),
            showLegend: true
         }));
        this.addChart('chart.severity.name', new SeverityChart({collection}));
        this.addChart('chart.duration.name', new DurationChart({collection}));
    }

    addChart(name, chart) {
        const container = $(`<div class="chart__wrap">
            <div class="chart island">
                <h2 class="chart__title">${t(name, {})}</h2>
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
            skipped: 0,
            passed: 0,
            unknown: 0
        });
    }
}

export default GraphsView;
