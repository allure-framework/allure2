import template from './DurationWidgetView.hbs';
import {View} from 'backbone.marionette';
import {className, regions} from '../../decorators';
import DurationChartView from '../../components/graph-duration-chart/DurationChartView';

@className('duration-widget')
@regions({
    chart: '.duration-widget__content'
})
class DurationWidgetView extends View {
    template = template;

    onRender() {
        this.showChildView('chart', new DurationChartView({
            model: this.model.get('items'),
        }));
    }
}

export default DurationWidgetView;
