import template from './SeverityWidgetView.hbs';
import {View} from 'backbone.marionette';
import {className, regions} from '../../decorators';
import SeverityChartView from '../../components/graph-severity-chart/SeverityChartView';


@className('severity-widget')
@regions({
    chart: '.severity-widget__content'
})
class SeverityWidgetView extends View {
    template = template;

    onRender() {
        this.showChildView('chart', new SeverityChartView({
            model: this.model.get('items'),
        }));
    }
}

export default SeverityWidgetView;
