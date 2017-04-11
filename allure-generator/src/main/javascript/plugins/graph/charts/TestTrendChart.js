import {scaleLinear} from 'd3-scale';
import {max, sum} from 'd3-array';
import {entries, set} from 'd3-collection';
import {values} from '../../../util/statuses';
import BaseChartView from '../../../components/chart/BaseChartView';
import {area, stack} from 'd3-shape';

const Y_AMOUNT = 8;

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
        if (data && data.length) {
            this.doShow(data);
        } else {
            this.$el.html('<div class="widget__noitems">There are nothing to show</div>');
        }

        // const s = stack()
        //     .keys(values)
        //     .value((d, key) => d[key] || 0);
        //
        // const a = area()
        //     .x(d => this.x(d.data.id))
        //     .y0(d => this.y(d[0]))
        //     .y1(d => this.y(d[1]));
        //
        // this.y.domain([0, max(
        //     data,
        //     d => sum(entries(d), l => set(values).has(l.key) ? l.value : 0))
        // ]);
        // this.x.domain(extent(data, d => d.id));
        //
        // this.makeBottomAxis(this.svg.select('.chart__axis_x'), {
        //         scale: this.x,
        //         tickFormat: d => '#' + d
        //     }, {
        //         top: 400,
        //         left: 15
        //     }
        // );
        // this.makeLeftAxis(this.svg.select('.chart__axis_y'), {
        //     scale: this.y,
        //     ticks: Y_AMOUNT
        // }, {
        //     left: 400,
        //     top: 15
        // });

        // this.svg.select('.chart__plot').attrs({transform: `translate(${PAD_LEFT},${PAD_TOP})`});
        //
        // const layer = this.svg
        //     .selectAll('.layer')
        //     .data(s(data))
        //     .enter()
        //     .append('g')
        //     .attr('class', 'layer');
        //
        // layer.append('path')
        //     .attr('class', 'area')
        //     .attr('d', a)
        //     .attr('class', d => 'trend_status_' + d.key)
        //     .style('opacity', .8);

        super.onAttach();
    }

    doShow(data) {
        const width = 600;
        const height = 300;

        console.log(width);
        console.log(height);

        this.x.range([0, width]);
        this.y.range([height, 0], 1);

        this.svg = this.setupViewport();

        this.x.domain([0, data.length]).nice();

        this.makeBottomAxis(this.svg.select('.chart__axis_x'), {
            scale: this.x,
            tickFormat: d => '#' + d
        }, {
            top: PAD_TOP + height,
            left: PAD_LEFT
        });

        const maxY = max(data, d => d.total);
        this.y.domain([0, maxY]);

        this.makeLeftAxis(this.svg.select('.chart__axis_y'), {
            scale: this.y,
            ticks: Math.min(10, maxY)
        }, {
            left: PAD_LEFT,
            top: PAD_TOP
        });

        this.svg.select('.chart__plot').attrs({transform: `translate(${PAD_LEFT},${PAD_TOP})`});

        const s = stack()
            .keys(values)
            .value((d, key) => d[key] || 0);

        const a = area()
            .x(d => this.x(d.data.id))
            .y0(d => this.y(d[0]))
            .y1(d => this.y(d[1]));

        const layer = this.svg
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
        return this.options.items;
    }
}

export default TestTrendGraphView;
