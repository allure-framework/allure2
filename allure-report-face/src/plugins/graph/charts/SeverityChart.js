import BaseChartView from '../../../components/chart/BaseChartView';
import PopoverView from '../../../components/popover/PopoverView';
import escape from '../../../util/escape';
import d3 from 'd3';

const PAD_LEFT = 30;
const PAD_TOP = 7;
const PAD_BOTTOM = 30;

const severities = ['BLOCKER', 'CRITICAL', 'NORMAL', 'MINOR', 'TRIVIAL'];
const statuses = ['FAILED', 'BROKEN', 'CANCELED', 'PENDING', 'PASSED'];

export default class SeverityChart extends BaseChartView {

    initialize() {
        this.x = d3.scale.ordinal().domain(severities);
        this.y = d3.scale.sqrt();
        this.status = d3.scale.ordinal().domain(statuses);
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

    onShow() {
        const data = this.getChartData();
        this.$el.height(this.$el.width() * 0.5);
        const width = this.$el.width() - PAD_LEFT;
        const height = this.$el.height() - PAD_BOTTOM - PAD_TOP;

        this.x.rangeRoundBands([0, width], 0.2);
        this.y.range([height, 0], 1);
        this.y.domain([0, d3.max(data, d => d3.max(d, d => d.value))]).nice();
        this.status.rangeRoundBands([0, this.x.rangeBand()]);

        this.svg = this.setupViewport();

        this.makeAxis(this.svg.select('.chart__axis_x'), {
            tickFormat: d => d.toLowerCase(),
            orient: 'bottom',
            scale: this.x
        }, {
            top: height + PAD_TOP,
            left: PAD_LEFT
        });
        this.makeAxis(this.svg.select('.chart__axis_y'), {
            orient: 'left',
            scale: this.y
        }, {
            left: PAD_LEFT,
            top: PAD_TOP
        });
        this.svg.select('.chart__plot').attr({transform: `translate(${PAD_LEFT},${PAD_TOP})`});

        var bars = this.svg.select('.chart__plot').selectAll('.chart__group')
            .data(data).enter()
            .append('g')
            .attr('transform', d => `translate(${this.x(d[0].severity)},0)`)
            .selectAll('.bar')
            .data(d => d).enter()
            .append('rect');

        bars.attr({
            x: d => this.status(d.status),
            y: height,
            height: 0,
            width: this.status.rangeBand(),
            'class': d => 'chart__bar chart__fill_status_' + d.status
        });

        this.bindTooltip(bars);

        if(this.firstRender) {
            bars = bars.transition().duration(500);
        }

        bars.attr({
            y: d => this.y(d.value),
            height: d => height - this.y(d.value)
        });
        super.onShow();
    }


    getTooltipContent({value, severity, status, testcases}) {
        const LIST_LIMIT = 10;
        const items = testcases.slice(0, LIST_LIMIT);
        const overLimit = testcases.length - items.length;
        return `<b>${value} ${severity.toLowerCase()} test cases ${status.toLowerCase()}</b><br>` +
            `<ul class="popover__list">` +
                items.map(testcase => escape`<li>${testcase.title}</li>`).join('') +
            `</ul>` +
            (overLimit ? `...and ${overLimit} more` : '');
    }
}
