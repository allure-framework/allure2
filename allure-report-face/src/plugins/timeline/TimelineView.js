import './styles.css';
import BaseChartView from '../../components/chart/BaseChartView';
import {once} from 'underscore';
import {className} from '../../decorators';
import duration from '../../helpers/duration';
import d3 from 'd3';
import escape from '../../util/escape';
import TooltipView from '../../components/tooltip/TooltipView';

const BAR_HEIGHT = 30;
const BAR_GAP = 1;

const PAD_BOTTOM = 30;
const PAD_LEFT = 10;
const PAD_RIGHT = 10;

@className('timeline')
class TimelineView extends BaseChartView {
    initialize() {
        this.x = d3.scale.linear();
        this.tooltip = new TooltipView({position: 'bottom'});
    }

    onShow(waitTransition) {
        if(waitTransition || this.firstRender) {
            const callback = once(() => this.doShow());
            this.$el.parent().one('transitionend', callback);
            setTimeout(callback, 500);
        } else {
            this.doShow();
        }
    }

    doShow() {
        const width = this.$el.width() - PAD_LEFT - PAD_RIGHT;

        const [from, to] = this.model.getAllTestcases().reduce(
            ([from, to], {time}) => [Math.min(from, time.start), Math.max(to, time.stop)],
            [Number.POSITIVE_INFINITY, Number.NEGATIVE_INFINITY]
        );
        this.minX = from;

        this.x.domain([0, to - from]).nice();
        this.x.range([0, width]);


        this.svg = this.setupViewport();

        var currentHeight = 0;
        this.model.get('hosts').forEach(host => {
            currentHeight += this.drawHost(host, currentHeight);
        });

        this.makeAxis(this.svg.select('.chart__axis_x'), {
            tickFormat: d => duration(d, 2),
            scale: this.x,
            orient: 'bottom'
        }, {
            top: currentHeight,
            left: PAD_LEFT
        });
        this.$el.height(currentHeight + PAD_BOTTOM);
        super.onShow();
    }

    drawHost(host, offset) {
        const startTime = this.minX;
        const height = (host.threads.length + 1) * BAR_HEIGHT;
        const testcases = host.threads.reduce((testcases, thread) =>
                testcases.concat(thread.testCases.map(({time, uid, title, status}) => {
                    return {
                        uid, title, status,
                        start: time.start - startTime,
                        stop: time.stop - startTime,
                        thread: thread.title
                    };
                })),
            []
        );
        const y = d3.scale.ordinal()
            .domain([''].concat(host.threads.map(d => d.title)))
            .rangeRoundBands([0, height]);
        const group = this.svg.select('.chart__plot').append('g').attr({
            'class': 'timeline__group',
            transform: `translate(${PAD_LEFT},${offset})`
        });

        group.append('rect').attr({
            'class': 'timeline__host-bg',
            width: this.x.range()[1] - this.x.range()[0],
            height: y.range()[1]
        });
        group.append('text').text(host.title).attr({
            'class': 'timeline__host',
            y: BAR_HEIGHT * 0.7,
            x: 10
        });

        var bars = group.selectAll('.timeline__item')
            .data(testcases).enter()
            .append('a')
            .attr({
                'xlink:href': d => '#timeline/' + d.uid
            })
            .append('rect')
            .attr({
                'class': d => 'timeline__item chart__fill_status_' + d.status,
                rx: 2, ry: 2,
                width: 0,
                height: BAR_HEIGHT - BAR_GAP * 2,
                x: this.x(0),
                y: d => y(d.thread) + BAR_GAP
            });
        this.bindTooltip(bars);
        bars.on('click', this.hideTooltip.bind(this));
        if(this.firstRender) {
            bars = bars.transition().duration(500);
        }
        bars.attr({
            x: d => this.x(d.start),
            width: d => this.x(d.stop - d.start)
        });
        this.makeAxis(group.append('g').classed('chart__axis', true), {
            tickSize: 0,
            tickFormat: '',
            scale: y,
            orient: 'left'
        });
        return height;
    }

    getTooltipContent(d) {
        return escape`${d.title}<br>${duration(d.start)} — ${duration(d.stop)}`;
    }
}

export default TimelineView;
