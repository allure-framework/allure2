import './styles.css';
import BaseChartView from '../../components/chart/BaseChartView';
import {className} from '../../decorators';
import duration from '../../helpers/duration';
import translate from '../../helpers/t';

import {scaleLinear, scaleBand} from 'd3-scale';
import {select, event as currentEvent} from 'd3-selection';
import {brushX} from 'd3-brush';
import {drag} from 'd3-drag';
import 'd3-selection-multi';
import escape from '../../util/escape';
import TooltipView from '../../components/tooltip/TooltipView';
import template from './TimelineView.hbs';
import {axisBottom} from 'd3-axis';


const HOST_TITLE_HEIGHT = 20;
const BRUSH_HEIGHT = 12;
const BAR_HEIGHT = 15;
const BAR_GAP = 2;
const PADDING = 30;


@className('timeline')
class TimelineView extends BaseChartView {
    PADDING = 30;
    BAR_HEIGHT = 15;

    initialize() {
        this.chartX = scaleLinear();
        this.brushX = scaleLinear();
        this.brush = brushX().on('brush', this.onBrushChange.bind(this));
        this.tooltip = new TooltipView({position: 'bottom'});
        this.minDuration = this.model.get('time').minDuration;
        this.maxDuration = this.model.get('time').maxDuration;
        this.selectedDuration = this.minDuration;
        this.data = this.model.getFilteredData(this.minDuration);
    }

    onAttach() {
        this.doShow();
    }

    setupViewport() {
        this.$el.html(template(this));
        this.svgChart = select(this.$el[0]).select('.timeline__chart_svg');
        this.svgBrush = select(this.$el[0]).select('.timeline__brush_svg');
        this.slider = this.svgChart.select('.timeline__slider');
    }

    setupSlider() {
        var sliderScale = scaleLinear()
            .range([0, this.width])
            .domain([this.minDuration, this.maxDuration])
            .clamp(true);

        this.slider.append('line')
            .attrs({
                class: 'timeline__slider_track',
                x1: sliderScale.range()[0],
                x2: sliderScale.range()[1]
            });

        this.handle = this.slider.insert('circle')
            .attrs({
                class: 'timeline__slider_handle',
                cx: sliderScale(this.selectedDuration),
                r: 8
            })
            .call(drag()
                .on('drag', () => {
                    this.selectedDuration = sliderScale.invert(currentEvent.x);
                    this.handle.attr('cx', sliderScale(this.selectedDuration));
                })
                .on('end', () => {
                    this.data = this.model.getFilteredData(this.selectedDuration);
                    this.doShow();
                    this.handle.attr('cx', sliderScale(this.selectedDuration));
                })
            );

        const opts = {
            count: this.data.selectedTestCases,
            percent: (100 * this.data.selectedTestCases / this.model.get('statistic').total).toFixed(2),
            duration: duration(this.selectedDuration)
        };
        this.slider.insert('g')
            .append('text')
            .attr('transform', `translate(${this.width/2}, 20)`)
            .attr('class', 'timeline__slider_text')
            .text(translate('tab.timeline.selected', {hash: opts}));

        this.slider.insert('g')
            .attr('class', 'timeline__slider_text')
            .attr('transform', 'translate(0, ' + 20 + ')')
            .selectAll('text')
            .data(sliderScale.domain())
            .enter().append('text')
            .attr('x', sliderScale)
            .text(d => duration(d, 1));
    }

    doShow() {
        this.width = this.$el.width() > 2 * PADDING ? this.$el.width() - 2 * PADDING : this.$el.width();

        this.minX = this.model.get('time').start;
        this.maxX = this.model.get('time').duration;

        this.chartX.domain([0, this.maxX]);
        this.chartX.range([0, this.width]);

        this.brushX.domain([0, this.maxX]);
        this.brushX.range([0, this.width]);

        this.setupViewport();
        this.setupSlider();

        var height = 0;
        this.data['children'].forEach(host => {
            height += this.drawHost(host, height);
        });

        select(this.$el[0]).select('.timeline__brush')
            .style('top', () => { return Math.min(this.$el.height() - BRUSH_HEIGHT, height + 2*PADDING) + 'px'; });

        this.xChartAxis = this.makeAxis(
            axisBottom(),
            this.svgChart.select('.timeline__chart__axis_x'),
            {
                scale: this.chartX,
                tickFormat: () => '',
                tickSizeOuter: 0,
                tickSizeInner: height + BAR_HEIGHT + 6
            }
        );

        this.xBrushAxis = this.makeAxis(
            axisBottom(),
            this.svgBrush.select('.timeline__brush__axis_x'),
            {
                scale: this.chartX,
                tickFormat: d => duration(d, 2),
                tickSizeOuter: 0
            },
            {
                top: BRUSH_HEIGHT + BAR_GAP + 6,
                left: PADDING
            }
        );

        this.brush.extent([[0, 0], [this.width, BRUSH_HEIGHT]]);
        this.svgBrush.append('g')
            .attrs({ transform: `translate(${PADDING}, 4)` })
            .attr('class', 'brush')
            .call(this.brush)
            .call(this.brush.move, this.chartX.range());


        if(this.firstRender) {
            this.svgBrush.select('.brush')
                .transition().duration(300)
                .call(this.brush.move, [1/16 * this.chartX(this.maxX), 15/16 * this.chartX(this.maxX)])
                .transition().duration(500)
                .call(this.brush.move, this.chartX.range());
        }

        this.svgChart.attr('height', () => { return height + 2*BAR_HEIGHT + 6+ PADDING; });

        super.onRender();
    }

    drawHost(host, offset) {
        const height = host.children.length * (BAR_GAP + BAR_HEIGHT) + HOST_TITLE_HEIGHT;
        const testCases = this.model.getTestcases(host);

        const y = scaleBand()
            .domain(host.children.map(d => d.name))
            .range([HOST_TITLE_HEIGHT, height]);

        const group = this.svgChart.select('.timeline__plot').append('g').attrs({
            'class': 'timeline__group',
            transform: `translate(0, ${offset})`
        });

        var rect = group.append('rect').attrs({
             'class': 'timeline__host-bg',
             x: -BAR_GAP,
             y: BAR_GAP,
             height: HOST_TITLE_HEIGHT - 2 * BAR_GAP
        });

        var text = group.append('text').text(host.name).attrs({
            'class': 'timeline__host',
            x: BAR_GAP,
            y: HOST_TITLE_HEIGHT/2
        });

        rect.attr('width', () => {return text.node().getComputedTextLength() + 4 * BAR_GAP;});

        var bars = group.selectAll('.timeline__item ')
            .data(testCases).enter()
            .append('a')
            .attrs({
                'xlink:href': d => '#testcase/' + d.uid
            })
            .append('rect')
            .attrs({
                'class': d => 'timeline__item chart__fill_status_' + d.status,
                x: d => this.chartX(d.start),
                y: d => y(d.thread) + BAR_GAP,
                rx: 2,
                ry: 2,
                width: d => this.chartX(d.stop) - this.chartX(d.start),
                height: BAR_HEIGHT
            });

        this.bindTooltip(bars);
        bars.on('click', this.hideTooltip.bind(this));

        return height + 4 * BAR_GAP;
    }

    onBrushChange() {
        var selection = currentEvent.selection;
        var maxRight = this.chartX.range()[1];

        if (selection) {
            this.chartX.domain(selection.map(this.brushX.invert, this.brushX));
            this.svgChart.selectAll('.timeline__item').attrs({
                x: d => Math.max(0, Math.min(this.chartX(d.start), maxRight)),
                width: d => Math.max(0, Math.min(this.chartX(d.stop), maxRight)) - Math.max(0, Math.min(this.chartX(d.start), maxRight))
            });
            this.svgBrush.select('.timeline__brush__axis_x').call(this.xBrushAxis);
            this.svgChart.select('.timeline__chart__axis_x').call(this.xChartAxis);
        }
    }

    getTooltipContent(d) {
        return escape`${d.name}<br>${duration(d.start)} — ${duration(d.stop)}`;
    }
}

export default TimelineView;
