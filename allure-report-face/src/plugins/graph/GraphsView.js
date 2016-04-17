import './styles.css';
import {LayoutView} from 'backbone.marionette';
import $ from 'jquery';
import {className} from '../../decorators';
import * as charts from './charts';

@className('charts-grid')
class GraphsView extends LayoutView {
    template() { return ''; }

    onShow() {
        Object.keys(charts).forEach(chart => this.addChart(chart, charts[chart]));
    }

    addChart(name, Chart) {
        const container = $(`<div class="chart__wrap">
            <div class="chart island">
                <h2 class="chart__title">${name}</h2>
                <div class="chart__body"></div>
            </div>
        </div>`);
        this.$el.append(container);
        this.addRegion(name, {el: container.find('.chart__body')});
        this.getRegion(name).show(new Chart({collection: this.collection}));
    }
}

export default GraphsView;
