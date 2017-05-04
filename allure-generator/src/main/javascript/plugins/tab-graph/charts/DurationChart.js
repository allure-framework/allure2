import BaseChartView from '../../../components/chart/BaseChartView';
import {scaleLinear, scaleSqrt} from 'd3-scale';
import {histogram, max, median} from 'd3-array';
import PopoverView from '../../../components/popover/PopoverView';
import escape from '../../../util/escape';
import duration from '../../../helpers/duration';
import translate from '../../../helpers/t';


export default class DurationChart extends BaseChartView {

    initialize() {
        this.x = scaleLinear();
        this.y = scaleSqrt();
        this.tooltip = new PopoverView({position: 'right'});
        this.data = this.getChartData();
    }

    getChartData() {
        return this.data = this.collection.toJSON().map(testcase => ({
            value: testcase.time.duration,
            name: testcase.name
        })).filter(testcase => {
            return testcase.value !== null;
        });
    }

    onAttach() {
        if (this.data.length) {
            this.doShow();
        } else {
            this.$el.html(`<div class="widget__noitems">${translate('chart.duration.empty')}</div>`);
        }
        super.onAttach();
    }

    doShow() {
        this.setupViewport();

        this.x.range([0, this.width]);
        this.y.range([this.height, 0], 1);

        const maxDuration = max(this.data, d => d.value);
        this.x.domain([0, Math.max(maxDuration, 10)]).nice();

        const bins = histogram()
           .value(d => d.value)
           .domain(this.x.domain())
           .thresholds(this.x.ticks())(this.data)
           .map(bin => ({
               x0: bin.x0,
               x1: bin.x1,
               y: bin.length,
               testcases: bin
        }));

        const maxY = max(bins, d => d.y);

        this.y.domain([0, maxY]).nice();

        this.makeBottomAxis({
            scale: this.x,
            tickFormat: time => duration(time, 1)
        });

        this.makeLeftAxis({
            scale: this.y,
            ticks: Math.min(10, maxY)
        });

        const median_ = this.y(median(bins, d => d.y));
        var bars = this.plot.selectAll('.chart__bar')
            .data(bins).enter()
            .append('rect').classed('chart__bar', true);

        bars.attrs({
            x: d => this.x(d.x0) + 2,
            y: median_,
            width: d => this.x(d.x1) - this.x(d.x0)-3,
            height: this.height - median_
        });

        this.bindTooltip(bars);

        if(this.firstRender) {
            bars = bars.transition().duration(500);
        }

        bars.attrs({
            y: d => this.y(d.y),
            height: d => this.height - this.y(d.y)
        });
    }


    getTooltipContent({testcases}) {
        const LIST_LIMIT = 10;
        const items = testcases.slice(0, LIST_LIMIT);
        const overLimit = testcases.length - items.length;
        return `<b>${testcases.length} test cases</b><br>
            <ul class="popover__list">
                ${items.map(testcase => escape`<li>${testcase.name}</li>`).join('')}
            </ul>
            ${overLimit ? `...and ${overLimit} more` : ''}
        `;
    }
}
