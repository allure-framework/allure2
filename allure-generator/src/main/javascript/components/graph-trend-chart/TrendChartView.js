import './styles.css';
import {scaleLinear, scalePoint} from 'd3-scale';
import {max} from 'd3-array';
import {area, stack} from 'd3-shape';
import {values} from '../../util/statuses';
import translate from '../../helpers/t';
import BaseChartView from '../../components/graph-base/BaseChartView';
import TooltipView from '../../components/tooltip/TooltipView';
import trendTooltip from './trend-tooltip.hbs';


class TrendChartView extends BaseChartView {

    initialize() {
        this.x = scalePoint();
        this.y = scaleLinear();
        this.tooltip = new TooltipView({position: 'top'});
    }

    getChartData() {
        return this.options.items.map((item, i) => ({
            ...item,
            id:  item.buildOrder ? `build_${item.buildOrder}` : `item_${i}`,
            name: item.buildOrder ? `#${item.buildOrder}` : `${i+1}(?)`
        }));
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
        data = data.reverse();
        this.setupViewport();
        this.x.range([0, this.width]);
        this.y.range([this.height, 0]);

        const maxY = max(data, d => d.statistic.total);

        this.x.domain(data.map(d => d.name));
        this.y.domain([0, maxY]);

        const s = stack()
            .keys(values)
            .value((d, key) => {
                return d.statistic[key] || 0;
            });

        const a = area()
            .x(d => this.x(d.data.name))
            .y0(d => this.y(d[0]))
            .y1(d => this.y(d[1]));

        this.makeBottomAxis({
            scale: this.x,
        });

        this.makeLeftAxis({
            scale: this.y,
        });

        const layer = this.plot
            .selectAll('.layer')
            .data(s(data))
            .enter()
            .append('g')
            .attr('class', 'layer');

        layer.append('path')
            .attr('class', 'area')
            .attr('d', a)
            .attr('class', d => `trend__area_status_${d.key}`)
            .style('opacity', .85);

        const slice = this.plot
            .selectAll('.edge')
            .data(data)
            .enter()
            .append('g')
            .attr('class', 'slice');

        slice.filter((d) => d.reportUrl)
            .append('a')
            .attr('xlink:href', d => d.reportUrl)
            .attr('class', 'edge');

        slice.filter((d) => !d.reportUrl)
            .append('g')
            .attr('class', 'edge');

        this.plot.selectAll('.edge')
            .append('line')
            .attr('id', d => d.id)
            .attr('x1', d => this.x(d.name))
            .attr('y1', d => this.y(d.statistic.total))
            .attr('x2', d => this.x(d.name))
            .attr('y2', this.y(0))
            .attr('stroke', 'white')
            .attr('stroke-width', 1)
            .attr('class', 'report-line');

        this.plot.selectAll('.edge')
            .append('rect')
            .style('opacity', .0)
            .attr('class', 'report-edge')
            .attr('x', (d, i) => i > 0 ? this.x(d.name) - this.x.step() / 2 : 0)
            .attr('y', 0)
            .attr('height', this.height)
            .attr('width', (d, i) => i === 0 || this.x(d.name) === this.width ? this.x.step() / 2 : this.x.step())
            .on('mouseover', (d) => {
                const anchor = this.plot.select(`.report-line#${d.id}`).node();
                this.showTooltip(d, anchor);
            })
           .on('mouseout', () => this.hideTooltip());
    }

    getTooltipContent(data) {
        return trendTooltip(data);
    }
}

export default TrendChartView;
