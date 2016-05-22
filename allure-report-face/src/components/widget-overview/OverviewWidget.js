import './styles.css';
import {ItemView} from 'backbone.marionette';
import template from './OverviewWidget.hbs';
import c3 from 'c3';
import {colors, states} from '../../util/statuses';

export default class OverviewWidget extends ItemView {
    template = template;

    onAttach() {
        let fill = states.map((state) => [state, this.model.get('statistic')[state]]);

        this.chart = c3.generate({
            bindto: '.overview-widget-graph',
            color: {
                pattern: colors
            },
            size: {
                height: 150,
                width: 150
            },
            padding: {
                left: 30,
                right: 15,
                top: 0,
                bottom: 0
            },
            data: {
                type: 'donut',
                columns: fill
            },
            legend: {
                show: false
            },
            donut: {
                width: 10,
                title: `${(this.model.get('statistic')['passed'] / this.model.get('statistic')['total'] * 100).toFixed(1)}%`,
                label: {
                    threshold: 0.00000001,
                    show: false
                }
            },
            tooltip: {
                grouped: false,
                format: {
                    value: function (value, ratio) {
                        return `${value} (${(ratio * 100).toFixed(1)}%)`;
                    }
                }
            }
        }); 
    }
}
