import BaseChartView from '../../../components/chart/BaseChartView';
import TooltipView from '../../../components/tooltip/TooltipView';
import {on} from '../../../decorators';
import d3 from 'd3';
import escape from '../../../util/escape';

const legendTpl = `<div class="chart__legend">
    ${['Failed', 'Broken', 'Canceled', 'Pending', 'Passed'].map((status) =>
        `<p class="chart__legend-row" data-status="${status.toUpperCase()}"><span class="chart__legend-icon chart__legend-icon_status_${status.toUpperCase()}"></span> ${status}</p>`
    ).join('')}
</div>`;

export default class StatusChart extends BaseChartView {

    initialize() {
        this.arc = d3.svg.arc().innerRadius(0);
        this.pie = d3.layout.pie().sort(null).value(d => d.value);
        this.tooltip = new TooltipView({position: 'center'});
    }

    getChartData() {
        const stats = this.collection.reduce((stats, testcase) => {
            stats[testcase.get('status')]++;
            return stats;
        }, {
            FAILED: 0,
            BROKEN: 0,
            CANCELED: 0,
            PENDING: 0,
            PASSED: 0
        });
        return Object.keys(stats).map(key => ({
            name: key,
            value: stats[key],
            part: stats[key] / this.collection.length
        }));
    }


    setupViewport() {
        const svg = super.setupViewport();
        this.$el.append(legendTpl);
        return svg;
    }

    onShow() {
        const data = this.getChartData();
        const width = this.$el.width();
        const radius = width / 4;
        this.$el.height(radius * 2);
        this.arc.outerRadius(radius);

        this.svg = this.setupViewport();

        var sectors = this.svg.select('.chart__plot')
            .attr({transform: `translate(${width / 2 - 70},${radius})`})
            .selectAll('.chart__arc').data(this.pie(data))
            .enter()
            .append('path')
            .attr('class', d => 'chart__arc chart__fill_status_' + d.data.name);

        this.bindTooltip(sectors);

        if(this.firstRender) {
            sectors.transition().duration(750).attrTween('d', d => {
                const radiusFn = d3.interpolate(10, radius);
                const startAngleFn = d3.interpolate(0, d.startAngle);
                const endAngleFn = d3.interpolate(0, d.endAngle);
                return t =>
                    this.arc.outerRadius(radiusFn(t))({startAngle: startAngleFn(t), endAngle: endAngleFn(t)});
            });
        } else {
            sectors.attr('d', d => this.arc(d));
        }
        super.onShow();
    }

    getTooltipContent({data}) {
        return escape`
            ${data.value} tests (${(data.part * 100).toFixed(0)}%)<br>
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
