import BaseChartView from '../../../components/chart/BaseChartView';
import d3 from 'd3';
import PopoverView from '../../../components/popover/PopoverView';
import escape from '../../../util/escape';
import duration from '../../../helpers/duration';

const PAD_LEFT = 35;
const PAD_TOP = 7;
const PAD_BOTTOM = 30;

export default class DurationChart extends BaseChartView {

    initialize() {
        this.x = d3.time.scale.utc();
        this.y = d3.scale.sqrt();
        this.tooltip = new PopoverView({position: 'right'});
    }

    onShow() {
        const data = this.collection.toJSON().map(testcase => ({
            value: testcase.time.duration,
            title: testcase.title
        }));
        this.$el.height(this.$el.width() * 0.5);
        const width = this.$el.width() - PAD_LEFT;
        const height = this.$el.height() - PAD_BOTTOM - PAD_TOP;

        const maxDuration = d3.max(data, d => d.value);
        this.x.range([0, width]);
        this.y.range([height, 0], 1);

        this.x.domain([0, Math.max(maxDuration, 1)]);

        const bins = d3.layout.histogram()
            .value(d => d.value)
            .bins(this.x.ticks())(data)
            .map(bin => ({
                x: bin.x,
                y: bin.y,
                dx: bin.dx,
                testcases: bin
            }));

        const maxY = d3.max(bins, d => d.y);

        this.y.domain([0, maxY]).nice();

        this.svg = this.setupViewport();
        this.makeAxis(this.svg.select('.chart__axis_x'), {
            tickFormat: time => duration(time, 1),
            scale: this.x,
            orient: 'bottom'
        }, {
            top: PAD_TOP + height,
            left: PAD_LEFT
        });
        this.makeAxis(this.svg.select('.chart__axis_y'), {
            scale: this.y,
            orient: 'left'
        }, {
            left: PAD_LEFT,
            top: PAD_TOP
        });
        this.svg.select('.chart__plot').attr({transform: `translate(${PAD_LEFT},${PAD_TOP})`});

        const median = this.y(d3.median(bins, d => d.y));
        var bars = this.svg.select('.chart__plot').selectAll('.chart__bar')
            .data(bins).enter()
                .append('rect').classed('chart__bar', true);

        bars.attr({
            x: d => this.x(d.x) + 1,
            width: this.x(bins[0].dx) - 2,
            y: median,
            height: height - median
        });

        this.bindTooltip(bars);

        if(this.firstRender) {
            bars = bars.transition().duration(500);
        }

        bars.attr({
            y: d => this.y(d.y),
            height: d => height - this.y(d.y)
        });
        super.onShow();
    }


    getTooltipContent({testcases}) {
        const LIST_LIMIT = 10;
        const items = testcases.slice(0, LIST_LIMIT);
        const overLimit = testcases.length - items.length;
        return `<b>${testcases.length} test cases</b><br>` +
            `<ul class="popover__list">` +
            items.map(testcase => escape`<li>${testcase.title}</li>`).join('') +
            `</ul>` +
            (overLimit ? `...and ${overLimit} more` : '');
    }
}
