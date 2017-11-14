import './styles.scss';
import template from './CategoriesTrendWidgetView.hbs';
import {View} from 'backbone.marionette';
import {className, regions} from '../../decorators/index';
import TrendChartView from '../../components/graph-trend-chart/TrendChartView';
import {interpolateYlOrRd} from 'd3-scale-chromatic';
import {scaleOrdinal} from 'd3-scale';
import {range} from 'underscore';


@regions({
    chart: '.categories-trend__chart'
})
@className('categories-trend')
class CategoriesTrendWidgetView extends View {
    template = template;

    onRender() {
        const keys = this.model.sortedKeysByLastValue();
        const colors = scaleOrdinal(range(0, 1, 1/keys.length).map(d => interpolateYlOrRd(1-d)));
        this.showChildView('chart', new TrendChartView({
            model: this.model,
            keys,
            colors,
            hideLines: true,
            hidePoints: true
        }));
    }
}

export default CategoriesTrendWidgetView;
