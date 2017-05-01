import BaseChartView from '../../../components/chart/BaseChartView';
import PopoverView from '../../../components/popover/PopoverView';
import escape from '../../../util/escape';
import {values} from '../../../util/statuses';
import {scaleBand, scaleSqrt} from 'd3-scale';
import {max} from 'd3-array';


const severities = ['blocker', 'critical', 'normal', 'minor', 'trivial'];

export default class SeverityChart extends BaseChartView {

    initialize() {
        this.x = scaleBand().domain(severities);
        this.y = scaleSqrt();
        this.status = scaleBand().domain(values);
        this.tooltip = new PopoverView({position: 'right'});
    }

    getChartData() {
        return severities.map(severity =>
            values.map(status => {
                const testcases = this.collection.where({status, severity}).map(model => model.toJSON());
                return {
                    value: testcases.length,
                         testcases,
                         severity,
                         status
                };
            })
        );
    }

    onAttach() {
        this.setupViewport();
        const data = this.getChartData();

        this.x.range([0, this.width]);
        this.y.range([this.height, 0], 1);
        this.y.domain([0, max(data, d => max(d, d => d.value))]).nice();
        this.status.rangeRound([0, this.x.step()]);

        this.makeBottomAxis({
            tickFormat: d => d.toLowerCase(),
            scale: this.x
        });

        this.svg.selectAll('.tick').select('line')
            .attr('transform', 'translate(' + this.x.step()/2 + ', 0)');

        this.makeLeftAxis({
            scale: this.y,
            ticks: Math.min(10, this.y.domain()[1])
        });

        var bars = this.svg.select('.chart__plot').selectAll('.chart__group')
            .data(data).enter()
            .append('g')
            .attr('transform', d => `translate(${this.x(d[0].severity)},0)`)
            .selectAll('.bar')
            .data(d => d).enter()
            .append('rect');

        bars.attrs({
            x: d => this.status(d.status),
            y: this.height,
            height: 0,
            width: this.status.step(),
            'class': d => 'chart__bar chart__fill_status_' + d.status
        });

        this.bindTooltip(bars);

        if(this.firstRender) {
           bars = bars.transition().duration(500);
        }

        bars.attrs({
            y: d => this.y(d.value),
            height: d => this.height - this.y(d.value)
        });
        super.onAttach();
    }


    getTooltipContent({value, severity, status, testcases}) {
        const LIST_LIMIT = 10;
        const items = testcases.slice(0, LIST_LIMIT);
        const overLimit = testcases.length - items.length;
        return `<b>${value} ${severity.toLowerCase()} test cases ${status.toLowerCase()}</b><br>
            <ul class="popover__list">
                ${items.map(testcase => escape`<li>${testcase.name}</li>`).join('')}
            </ul>
            ${overLimit ? `...and ${overLimit} more` : ''}
        `;
    }
}
