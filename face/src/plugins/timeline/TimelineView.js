import './styles.css';
import BaseChartView from '../../components/chart/BaseChartView';
import {once} from 'underscore';
import {className} from '../../decorators';
import duration from '../../helpers/duration';

import {scaleLinear, scaleBand} from 'd3-scale';
import {event as currentEvent} from 'd3-selection';
import {brushX} from 'd3-brush';
import 'd3-selection-multi';

import escape from '../../util/escape';
import TooltipView from '../../components/tooltip/TooltipView';

const BAR_HEIGHT = 25;
const BAR_GAP = 0.5;
const MINIBAR_HEIGHT=5;

const PAD_BOTTOM = 30;
const PAD_LEFT = 25;
const PAD_RIGHT = 25;

const MINI_HEIGHT = 150;

@className('timeline')
class TimelineView extends BaseChartView {
    initialize() {
        this.x = scaleLinear();
        this.miniX = scaleLinear();
        this.tooltip = new TooltipView({position: 'bottom'});
        this.brush = brushX()
            .on('brush',this.onBrushChange.bind(this));
    }

    onAttach(waitTransition) {
        if(waitTransition || this.firstRender) {
            const callback = once(() => this.doShow());
            this.$el.parent().one('transitionend', callback);
            setTimeout(callback, 500);
        } else {
            this.doShow();
        }
    }

    doShow() {
        const width = this.$el.width() > PAD_LEFT + PAD_RIGHT ? this.$el.width() - PAD_LEFT - PAD_RIGHT : this.$el.width();

        const [from, to] = this.model.getAllTestcases().reduce(
            ([from, to], {time}) => [Math.min(from, time.start), Math.max(to, time.stop)],
            [Number.POSITIVE_INFINITY, Number.NEGATIVE_INFINITY]
        );
        this.minX = from;

        this.x.domain([0, to - from]).nice();
        this.x.range([0, width]);

        this.miniX.domain([0, to - from]).nice();
        this.miniX.range([0, width]);

        this.svg = this.setupViewport();

        var currentHeight = 0;
        this.model.get('children').forEach(host => {
            currentHeight += this.drawHost(host, currentHeight, false);
        });

        var mainHeight = currentHeight;
        currentHeight += PAD_BOTTOM;
        this.model.get('children').forEach(host => {
            currentHeight += this.drawHost(host, currentHeight, true);
        });

        this.xAxis = this.makeBottomAxis(this.svg.select('.chart__axis_x'), {
            tickFormat: d => duration(d, 2),
            scale: this.x
        }, {
            top: mainHeight,
            left: PAD_LEFT
        });

        this.svg.append('g').attr('class', 'chart__mini__axis_x');
        this.makeBottomAxis(this.svg.select('.chart__mini__axis_x'), {
            tickFormat: d => duration(d, 2),
            scale: this.miniX
        }, {
            top: currentHeight,
            left: PAD_LEFT
        });

        this.$el.height(currentHeight + PAD_BOTTOM);

        this.brush.extent([[0, currentHeight - MINI_HEIGHT + 2*PAD_BOTTOM], [width, currentHeight]]);
        this.svg.select('.chart__plot').append('g')
            .attrs({ transform: `translate(${PAD_LEFT},0)` })
            .attr('class', 'brush')
            .call(this.brush)
            .call(this.brush.move, this.x.range());

        super.onRender();
    }

    getTestcases(host) {
        const startTime = this.minX;
        return host.children.reduce((testcases, thread) =>
            testcases.concat(thread.children.map(({time, uid, name, status}) => {
                return {
                    uid, name, status,
                    start: time.start - startTime,
                    stop: time.stop - startTime,
                    thread: thread.name
                };
            })),
            []
        );
    }

    drawHost(host, offset, mini = false) {
        const itemClass = mini ? 'timeline__mini_item' : 'timeline__item';
        const barHeight = mini ?  MINIBAR_HEIGHT : BAR_HEIGHT;

        const height = (host.children.length + 1) * barHeight;
        const testcases = this.getTestcases(host);

        const y = scaleBand()
            .domain([''].concat(host.children.map(d => d.name)))
            .range([0, height]);

        const group = this.svg.select('.chart__plot').append('g').attrs({
            'class': 'timeline__group',
            transform: `translate(${PAD_LEFT},${offset})`
        });

        group.append('rect').attrs({
            'class': 'timeline__host-bg',
            width: Math.max(0, this.x.range()[1] - this.x.range()[0]),
            height: y.step()
        });

        if (!mini) {
            group.append('text').text(host.name).attrs({
                'class': 'timeline__host',
                y: barHeight * 0.7,
                x: 10
            });
        }

        var bars = group.selectAll('.' + itemClass)
            .data(testcases).enter()
            .append('a')
            .attrs({
                'xlink:href': d => '#timeline/' + d.uid
            })
            .append('rect')
            .attrs({
                'class': d => itemClass + ' chart__fill_status_' + d.status,
                rx: 1, ry: 1,
                width: 0,
                height: barHeight - BAR_GAP * 2,
                x: this.x(0),
                y: d => y(d.thread) + BAR_GAP
            });

        this.bindTooltip(bars);
        bars.on('click', this.hideTooltip.bind(this));

        if(this.firstRender) {
            bars = bars.transition().duration(500);
        }

        bars.attrs({
            x: d => this.x(d.start),
            width: d => this.x(d.stop) - this.x(d.start)
        });

        return height;
    }

    onBrushChange() {
        var selection = currentEvent.selection;
        var maxRight = this.x.range()[1];

        if (selection) {
            this.x.domain(selection.map(this.miniX.invert, this.miniX));
            this.svg.selectAll('.timeline__item').attrs({
                x: d => Math.max(0, Math.min(this.x(d.start), maxRight)),
                width: d => Math.max(0, Math.min(this.x(d.stop), maxRight)) - Math.max(0, Math.min(this.x(d.start), maxRight))
            });
            this.svg.select('.chart__axis_x').call(this.xAxis);
        }
    }

    getTooltipContent(d) {
        return escape`${d.name}<br>${duration(d.start)} — ${duration(d.stop)}`;
    }
}

export default TimelineView;
