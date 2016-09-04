import {View} from 'backbone.marionette';
import StatusChart from '../charts/StatusChart';
import {region} from '../../../decorators';
import template from './GraphWidget.hbs';

export default class GraphWidget extends View {
    template = template;

    @region('.graph-widget__chart')
    chart;

    onRender() {
        this.showChildView('chart', new StatusChart({
            statistic: this.model.get('statistic'),
            showLegend: false
        }));
    }
}
