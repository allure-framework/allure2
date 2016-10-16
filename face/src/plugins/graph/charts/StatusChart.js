import BaseChartView from '../../../components/chart/BaseChartView';
import TooltipView from '../../../components/tooltip/TooltipView';
import {on} from '../../../decorators';
import {omit} from 'underscore';
import d3 from 'd3';
import escape from '../../../util/escape';

const legendTpl = `<div class="chart__legend">
    ${['Failed', 'Broken', 'Canceled', 'Pending', 'Passed'].map((status) =>
        `<p class="chart__legend-row" data-status="${status.toLowerCase()}"><span class="chart__legend-icon chart__legend-icon_status_${status.toLowerCase()}"></span> ${status}</p>`
    ).join('')}
</div>`;

export default class StatusChart extends BaseChartView {

    initialize() {
        this.arc = d3.svg.arc();
        this.pie = d3.layout.pie().sort(null).value(d => d.value);
        this.tooltip = new TooltipView({position: 'center'});
    }

    getChartData() {
        const {total} = this.options.statistic;
        const stats = omit(this.options.statistic, 'total');
        return Object.keys(stats).map(key => ({
            name: key.toUpperCase(),
            value: stats[key],
            part: stats[key] / total
        }));
    }

    setupViewport() {
        const svg = super.setupViewport();
        if(this.options.showLegend) {
            this.$el.append(legendTpl);
        }
        return svg;
    }

    onAttach() {
        const data = this.getChartData();
        const width = this.$el.width();
        const radius = width / 4;
        var leftOffset = width / 2;

        if(this.options.showLegend) {
            leftOffset -= 70;
        }
        this.$el.height(radius * 2);
        this.arc.innerRadius(0.8 * radius).outerRadius(radius);

        this.svg = this.setupViewport();

        var sectors = this.svg.select('.chart__plot')
            .attr({transform: `translate(${leftOffset},${radius})`})
            .selectAll('.chart__arc').data(this.pie(data))
            .enter()
            .append('path')
            .attr('class', d => 'chart__arc chart__fill_status_' + d.data.name.toLowerCase());

        this.bindTooltip(sectors);

        this.svg.select('.chart__plot').append('text')
            .classed('chart__caption', true)
            .attr({dy: '0.4em'})
            .style({'font-size': radius / 3})
            .text(this.getChartTitle());

        if(this.firstRender) {
            sectors.transition().duration(750).attrTween('d', d => {
                const startAngleFn = d3.interpolate(0, d.startAngle);
                const endAngleFn = d3.interpolate(0, d.endAngle);
                return t =>
                    this.arc({startAngle: startAngleFn(t), endAngle: endAngleFn(t)});
            });
        } else {
            sectors.attr('d', d => this.arc(d));
        }
        super.onAttach();
    }

    formatNumber(number) {
        return (Math.floor(number * 100) / 100).toString();
    }

    getChartTitle() {
        const {passed, total} = this.options.statistic;
        return this.formatNumber((passed || 0) / total * 100) + '%';
    }

    getTooltipContent({data}) {
        return escape`
            ${data.value} tests (${this.formatNumber(data.part * 100)}%)<br>
            ${data.name}
        `;
    }

    @on('mouseleave .chart__legend-row')
    onLegendOut() {
        this.hideTooltip();
    }

    @on('mouseenter .chart__legend-row')
    onLegendHover(e) {
        const el = this.$(e.currentTarget);
        const status = el.data('status');
        const sector = this.$('.chart__fill_status_' + status)[0];
        const data = d3.select(sector).datum();
        this.showTooltip(data, sector);
    }
}
