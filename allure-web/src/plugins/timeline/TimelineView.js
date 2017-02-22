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


const HOST_TITLE_HEIGHT = 20;

const GRAPH_MIN_HEIGHT = 100;
const BAR_HEIGHT = 20;

const BRUSH_MIN_HEIGHT = 25;
const PREVIEW_BAR_HEIGHT = 2;

const MIN_BAR_GAP = 1;

const PADDING = 30;


@className('timeline')
class TimelineView extends BaseChartView {
    initialize() {
        this.x = scaleLinear();
        this.miniX = scaleLinear();
        this.tooltip = new TooltipView({position: 'bottom'});
        this.brush = brushX()
            .on('brush', this.onBrushChange.bind(this));
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
        const width = this.$el.width() > 2 * PADDING ? this.$el.width() - 2 * PADDING : this.$el.width();

        const [from, to] = this.model.getAllTestcases().reduce(
            ([from, to], {time}) => [Math.min(from, time.start), Math.max(to, time.stop)],
            [Number.POSITIVE_INFINITY, Number.NEGATIVE_INFINITY]
        );

        this.minX = from;
        this.maxX = to-from;

        this.x.domain([0, this.maxX]).nice();
        this.x.range([0, width]);

        this.miniX.domain([0, this.maxX]).nice();
        this.miniX.range([0, width]);

        this.svg = this.setupViewport();

        var totalThreads = this.model.get('children').reduce((a,b) => { return a + b.children.length; }, 0);

        var bar_gap = (GRAPH_MIN_HEIGHT - BAR_HEIGHT * totalThreads)/2;
        bar_gap = bar_gap < MIN_BAR_GAP ? MIN_BAR_GAP : Math.trunc(bar_gap);

        var preview_bar_gap = (BRUSH_MIN_HEIGHT - PREVIEW_BAR_HEIGHT * totalThreads)/2;
        preview_bar_gap = preview_bar_gap < MIN_BAR_GAP ? MIN_BAR_GAP : Math.trunc(preview_bar_gap);

        var currentHeight = 0;
        this.model.get('children').forEach(host => {
            currentHeight += this.drawHost(host, currentHeight, bar_gap);
        });

        var mainHeight = currentHeight;
        currentHeight += PADDING;
        this.model.get('children').forEach(host => {
            currentHeight += this.drawMiniHost(host, currentHeight, preview_bar_gap);
        });

        this.xAxis = this.makeBottomAxis(this.svg.select('.chart__axis_x'), {
            tickFormat: d => duration(d, 2),
            scale: this.x
        }, {
            top: mainHeight,
            left: PADDING
        });

        this.svg.append('g').attr('class', 'chart__mini__axis_x');
        this.makeBottomAxis(this.svg.select('.chart__mini__axis_x'), {
            tickFormat: d => duration(d, 2),
            scale: this.miniX
        }, {
            top: currentHeight + MIN_BAR_GAP,
            left: PADDING
        });

        const miniHeight = currentHeight - mainHeight;
        this.brush.extent([[0, mainHeight + PADDING -2*MIN_BAR_GAP], [width, currentHeight]]);
        this.svg.append('g')
            .attrs({ transform: `translate(${PADDING}, 0)` })
            .attr('class', 'brush')
            .call(this.brush)
            .call(this.brush.move, this.x.range());

        var handle = this.svg.select('.chart__plot')
            .append('g')
            .attrs({ transform: 'translate(' + (PADDING) + ', ' + (mainHeight + (miniHeight + PADDING)/2 - MIN_BAR_GAP) + ')'});

        handle.append('text')
            .attr('class', 'timeline__left_handle')
            .text(() => { return '\uf0d9'; });

        handle.append('text')
            .attr('class', 'timeline__right_handle')
            .attr('x', width)
            .text(() => { return '\uf0da'; });

        if(this.firstRender) {
            this.svg.select('.brush')
                .transition().duration(300)
                .call(this.brush.move, [1/16 * this.x(this.maxX), 15/16 * this.x(this.maxX)])
                .transition().duration(500)
                .call(this.brush.move, this.x.range());
        }

        this.svg.style('height', currentHeight + PADDING);
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

    drawHost(host, offset, bar_gap) {
        const height = host.children.length * (bar_gap + BAR_HEIGHT) + HOST_TITLE_HEIGHT;
        const testcases = this.getTestcases(host);

        const y = scaleBand()
            .domain(host.children.map(d => d.name))
            .range([HOST_TITLE_HEIGHT, height]);

        const group = this.svg.select('.chart__plot').append('g').attrs({
            'class': 'timeline__group',
            transform: `translate(${PADDING},${offset})`
        });

        var text = group.append('text').text(host.name).attrs({
            x: 0,
            y: HOST_TITLE_HEIGHT/2,
            'class': 'timeline__host',
            'dominant-baseline': 'central'
        });

        group.append('line').attrs({
             'class': 'timeline__host-bg', 
             x1: text.node().getComputedTextLength() + MIN_BAR_GAP,
             y1: HOST_TITLE_HEIGHT/2,
             x2: this.x.range()[1] - this.x.range()[0],
             y2: HOST_TITLE_HEIGHT/2
        });

        var bars = group.selectAll('.timeline__item ')
            .data(testcases).enter()
            .append('a')
            .attrs({
                'xlink:href': d => '#testcase/' + d.uid
            })
            .append('rect')
            .attrs({
                'class': d => 'timeline__item chart__fill_status_' + d.status,
                x: d => this.x(d.start),
                y: d => y(d.thread) + bar_gap,
                width: d => this.x(d.stop) - this.x(d.start),
                height: BAR_HEIGHT
            });

        this.bindTooltip(bars);
        bars.on('click', this.hideTooltip.bind(this));

        return height + bar_gap;
    }

    drawMiniHost(host, offset, preview_bar_gap) {
        const height = host.children.length * (preview_bar_gap + PREVIEW_BAR_HEIGHT);
        const testcases = this.getTestcases(host);

        const y = scaleBand()
            .domain(host.children.map(d => d.name))
            .range([0, height]);

        const group = this.svg.select('.chart__plot').append('g').attrs({
            'class': 'timeline__group',
            transform: `translate(${PADDING},${offset})`
        });

        group.selectAll('.timeline__mini_item')
            .data(testcases).enter()
            .append('rect')
            .attrs({
                'class': d => 'timeline__mini_item chart__fill_status_' + d.status,
                x: d => this.x(d.start),
                y: d => y(d.thread) + preview_bar_gap,
                width: d => this.x(d.stop) - this.x(d.start),
                height: PREVIEW_BAR_HEIGHT
            });

        return height + preview_bar_gap;
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

            this.svg.selectAll('.timeline__left_handle').attr('x', selection[0]);
            this.svg.selectAll('.timeline__right_handle').attr('x', selection[1]);
            this.svg.select('.chart__axis_x').call(this.xAxis);
        }
    }

    getTooltipContent(d) {
        return escape`${d.name}<br>${duration(d.start)} — ${duration(d.stop)}`;
    }
}

export default TimelineView;
