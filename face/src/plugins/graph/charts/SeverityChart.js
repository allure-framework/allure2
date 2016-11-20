import BaseChartView from '../../../components/chart/BaseChartView';
import PopoverView from '../../../components/popover/PopoverView';
import escape from '../../../util/escape';

import {scaleBand, scaleSqrt} from 'd3-scale';
import {max} from 'd3-array';

const PAD_LEFT = 30;
const PAD_TOP = 7;
const PAD_BOTTOM = 30;

const severities = ['blocker', 'critical', 'normal', 'minor', 'trivial'];
const statuses = ['failed', 'broken', 'canceled', 'pending', 'passed'];

export default class SeverityChart extends BaseChartView {

    initialize() {
        this.x = scaleBand().domain(severities);
        this.y = scaleSqrt();
        this.status = scaleBand().domain(statuses);
        this.tooltip = new PopoverView({position: 'right'});

    }

    getChartData() {
        return severities.map(severity =>
            statuses.map(status => {
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
        const data = this.getChartData();
        this.$el.height(this.$el.width() * 0.5);
        const width = this.$el.width() - PAD_LEFT - 2;
        const height = this.$el.height() - PAD_BOTTOM - PAD_TOP;

        this.x.range([0, width]);
        this.y.range([height, 0], 1);
        this.y.domain([0, max(data, d => max(d, d => d.value))]).nice();
        this.status.rangeRound([0, this.x.step()]);
        this.svg = this.setupViewport();

        this.makeBottomAxis(this.svg.select('.chart__axis_x'), {
            tickFormat: d => d.toLowerCase(),
            scale: this.x
        }, {
            top: height + PAD_TOP,
            left: PAD_LEFT
        });

        this.svg.selectAll('.tick').select('line')
            .attr('transform', 'translate(' + this.x.step()/2 + ', 0)');

        this.makeLeftAxis(this.svg.select('.chart__axis_y'), {
            scale: this.y
        }, {
            left: PAD_LEFT,
            top: PAD_TOP
        });
        this.svg.select('.chart__plot').attrs({transform: `translate(${PAD_LEFT},${PAD_TOP})`});

        var bars = this.svg.select('.chart__plot').selectAll('.chart__group')
            .data(data).enter()
            .append('g')
            .attr('transform', d => `translate(${this.x(d[0].severity)},0)`)
            .selectAll('.bar')
            .data(d => d).enter()
            .append('rect');

        bars.attrs({
            x: d => this.status(d.status),
            y: height,
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
            height: d => height - this.y(d.value)
        });
        super.onAttach();
    }


    getTooltipContent({value, severity, status, testcases}) {
        const LIST_LIMIT = 10;
        const items = testcases.slice(0, LIST_LIMIT);
        const overLimit = testcases.length - items.length;
        return `<b>${value} ${severity.toLowerCase()} test cases ${status.toLowerCase()}</b><br>` +
            `<ul class="popover__list">` +
                items.map(testcase => escape`<li>${testcase.name}</li>`).join('') +
            `</ul>` +
            (overLimit ? `...and ${overLimit} more` : '');
    }
}
