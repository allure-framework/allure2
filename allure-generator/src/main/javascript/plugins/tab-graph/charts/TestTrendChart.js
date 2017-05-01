import {scaleLinear} from 'd3-scale';
import {max} from 'd3-array';
import {values} from '../../../util/statuses';
import BaseChartView from '../../../components/chart/BaseChartView';
import {area, stack} from 'd3-shape';
import t from '../../../helpers/t';

const PAD_LEFT = 50;
const PAD_RIGHT = 15;
const PAD_TOP = 7;
const PAD_BOTTOM = 30;

class TestTrendGraphView extends BaseChartView {

    initialize() {
        this.x = scaleLinear();
        this.y = scaleLinear();
    }

    onAttach() {
        const data = this.getChartData();
        if (data && data.length > 1) {
            this.doShow(data);
        } else {
            this.$el.html(`<div class="widget__noitems">${t('chart.trend.empty')}</div>`);
        }
        super.onAttach();
    }

    doShow(data) {
        this.svg = this.setupViewport();
        const height = this.$el.outerHeight() - PAD_BOTTOM - PAD_TOP;
        const width = this.$el.outerWidth() - PAD_LEFT - PAD_RIGHT;

        const s = stack()
            .keys(values)
            .value((d, key) => d[key] || 0);

        const a = area()
            .x(d => this.x(d.data.id))
            .y0(d => this.y(d[0]))
            .y1(d => this.y(d[1]));

        this.x.range([0, width]);
        this.y.range([height, 0], 1);

        const maxY = max(data, d => d.total);

        this.x.domain([0, data.length - 1]).nice();
        this.y.domain([0, maxY]);

        this.makeBottomAxis(this.svg.select('.chart__axis_x'), {
            scale: this.x,
            tickFormat: d => '#' + d,
            ticks: data.length
        }, {
            top: PAD_TOP + height,
            left: PAD_LEFT
        });

        this.makeLeftAxis(this.svg.select('.chart__axis_y'), {
            scale: this.y,
            ticks: Math.min(10, maxY)
        }, {
            left: PAD_LEFT,
            top: PAD_TOP
        });

        const plot = this.svg.select('.chart__plot').attrs({transform: `translate(${PAD_LEFT},${PAD_TOP})`});

        const layer = plot
            .selectAll('.layer')
            .data(s(data))
            .enter()
            .append('g')
            .attr('class', 'layer');

        layer.append('path')
            .attr('class', 'area')
            .attr('d', a)
            .attr('class', d => 'trend_status_' + d.key)
            .style('opacity', .8);
    }

    getChartData() {
        const length = this.options.items.length - 1;
        this.options.items.forEach((d, i) => d['id'] = length - i);
        return this.options.items;
    }
}

export default TestTrendGraphView;
