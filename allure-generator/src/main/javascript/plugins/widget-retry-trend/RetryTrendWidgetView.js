import './styles.scss';
import template from './RetryTrendWidgetView.hbs';
import {View} from 'backbone.marionette';
import {className, regions} from '../../decorators/index';
import TrendChartView from '../../components/graph-trend-chart/TrendChartView';
import {scaleOrdinal} from 'd3-scale';
import {interpolateYlOrRd} from 'd3-scale-chromatic';


@regions({
    chart: '.retry-trend__chart'
})
@className('retry-trend')
export default class RetryTrendWidgetView extends View {
    template = template;

    onRender() {
        const {retry, run} = this.model.last().get('data');
        const retriesPercent = Math.min(retry, run)/run;
        const colors = scaleOrdinal(['#4682b4', interpolateYlOrRd(retriesPercent)]);
        this.showChildView('chart', new TrendChartView({
            model: this.model,
            keys: ['run', 'retry'],
            colors,
            hideLines: true,
            hidePoints: true
        }));
    }
}