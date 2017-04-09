import BaseChartView from '../../../components/chart/BaseChartView';

import {scaleLinear, scaleSqrt} from 'd3-scale';
import {histogram, max, median} from 'd3-array';
import PopoverView from '../../../components/popover/PopoverView';
import escape from '../../../util/escape';
import duration from '../../../helpers/duration';

const PAD_LEFT = 50;
const PAD_RIGHT = 15;
const PAD_TOP = 7;
const PAD_BOTTOM = 30;

export default class DurationChart extends BaseChartView {

    initialize() {
        this.x = scaleLinear();
        this.y = scaleSqrt();
        this.tooltip = new PopoverView({position: 'right'});
    }

    onAttach() {
        this.data = this.collection.toJSON().map(testcase => ({
            value: testcase.time.duration,
            name: testcase.name
        })).filter(testcase => {
            return testcase.value !== null;
        });

        if (this.data.length) {
            this.doShow();
        } else {
            this.$el.html('<div class="widget__noitems">There are nothing to show</div>');
        }

        super.onAttach();
    }

    doShow() {
        const width = this.$el.outerWidth() - PAD_LEFT - PAD_RIGHT;
        const height = this.$el.outerHeight() - PAD_BOTTOM - PAD_TOP;

        const maxDuration = max(this.data, d => d.value);
        this.x.range([0, width]);
        this.y.range([height, 0], 1);

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

        this.svg = this.setupViewport();
        this.makeBottomAxis(this.svg.select('.chart__axis_x'), {
            scale: this.x,
            tickFormat: time => duration(time, 1)
        }, {
            top: PAD_TOP + height,
            left: PAD_LEFT
        });
        this.makeLeftAxis(this.svg.select('.chart__axis_y'), {
            scale: this.y,
            ticks: Math.min(10, maxY)
        }, {
            left: PAD_LEFT,
            top: PAD_TOP
        });
        this.svg.select('.chart__plot').attrs({transform: `translate(${PAD_LEFT},${PAD_TOP})`});

        const median_ = this.y(median(bins, d => d.y));
        var bars = this.svg.select('.chart__plot').selectAll('.chart__bar')
            .data(bins).enter()
            .append('rect').classed('chart__bar', true);

        bars.attrs({
            x: d => this.x(d.x0) + 2,
            y: median_,
            width: d => this.x(d.x1)-this.x(d.x0)-3,
            height: height - median_
        });

        this.bindTooltip(bars);

        if(this.firstRender) {
            bars = bars.transition().duration(500);
        }

        bars.attrs({
            y: d => this.y(d.y),
            height: d => height - this.y(d.y)
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
