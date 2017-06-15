import './styles.css';
import {scaleLinear} from 'd3-scale';
import {max} from 'd3-array';
import {values} from '../../util/statuses';
import BaseChartView from '../../components/graph-base/BaseChartView';
import {area, stack} from 'd3-shape';
import translate from '../../helpers/t';


class TrendChartView extends BaseChartView {

    initialize() {
        this.x = scaleLinear();
        this.y = scaleLinear();
    }

    getChartData() {
        const length = this.options.items.length - 1;
        this.options.items.forEach((d, i) => d['id'] = length - i);
        return this.options.items;
    }

    onAttach() {
        const data = this.getChartData();
        if (data && data.length > 1) {
            this.doShow(data);
        } else {
            this.$el.html(`<div class="widget__noitems">${translate('chart.trend.empty')}</div>`);
        }
        super.onAttach();
    }

    doShow(data) {
        this.setupViewport();

        const s = stack()
            .keys(values)
            .value((d, key) => d[key] || 0);

        const a = area()
            .x(d => this.x(d.data.id))
            .y0(d => this.y(d[0]))
            .y1(d => this.y(d[1]));

        this.x.range([0, this.width]);
        this.y.range([this.height, 0], 1);

        const maxY = max(data, d => d.total);

        this.x.domain([0, data.length - 1]).nice();
        this.y.domain([0, maxY]);

        this.makeBottomAxis({
            scale: this.x,
            tickFormat: d => '#' + d,
            ticks: data.length
        });

        this.makeLeftAxis({
            scale: this.y,
            ticks: Math.min(10, maxY)
        });

        const layer = this.plot
            .selectAll('.layer')
            .data(s(data).reverse())
            .enter()
            .append('g')
            .attr('class', 'layer');

        layer.append('path')
            .attr('class', 'area')
            .attr('d', a)
            .attr('class', d => 'trend__area_status_' + d.key)
            .style('opacity', .85);
    }

}

export default TrendChartView;
