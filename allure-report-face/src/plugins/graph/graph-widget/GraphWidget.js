import {LayoutView} from 'backbone.marionette';
import StatusChart from '../charts/StatusChart';
import {region} from '../../../decorators';
import template from './GraphWidget.hbs';

export default class GraphWidget extends LayoutView {
    template = template;

    @region('.graph-widget__chart')
    chart;

    onShow() {
        this.chart.show(new StatusChart({
            statistic: this.model.get('statistic'),
            showLegend: false
        }));
    }
}
