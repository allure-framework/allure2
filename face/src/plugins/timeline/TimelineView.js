import './styles.css';
import BaseChartView from '../../components/chart/BaseChartView';
import {once} from 'underscore';
import {className} from '../../decorators';
import duration from '../../helpers/duration';

import {scaleLinear, scaleBand} from 'd3-scale';
import 'd3-selection-multi';

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
        this.x = scaleLinear();
        this.tooltip = new TooltipView({position: 'bottom'});
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

        this.svg = this.setupViewport();

        var currentHeight = 0;
        this.model.get('children').forEach(host => {
            currentHeight += this.drawHost(host, currentHeight);
        });

        this.makeBottomAxis(this.svg.select('.chart__axis_x'), {
            tickFormat: d => duration(d, 2),
            scale: this.x
        }, {
            top: currentHeight,
            left: PAD_LEFT
        });
        this.$el.height(currentHeight + PAD_BOTTOM);
        super.onRender();
    }

    drawHost(host, offset) {
        const startTime = this.minX;
        const height = (host.children.length + 1) * BAR_HEIGHT;
        const testcases = host.children.reduce((testcases, thread) =>
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
        group.append('text').text(host.name).attrs({
            'class': 'timeline__host',
            y: BAR_HEIGHT * 0.7,
            x: 10
        });

        var bars = group.selectAll('.timeline__item')
            .data(testcases).enter()
            .append('a')
            .attrs({
                'xlink:href': d => '#timeline/' + d.uid
            })
            .append('rect')
            .attrs({
                'class': d => 'timeline__item chart__fill_status_' + d.status,
                rx: 1, ry: 1,
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
        bars.attrs({
            x: d => this.x(d.start),
            width: d => this.x(Math.max(d.stop - d.start))
        });
        this.makeLeftAxis(group.append('g').classed('chart__axis', true), {
            tickSize: 0,
            tickFormat: '',
            scale: y
        });
        return height;
    }

    getTooltipContent(d) {
        return escape`${d.name}<br>${duration(d.start)} — ${duration(d.stop)}`;
    }
}

export default TimelineView;
