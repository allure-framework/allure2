import './styles.css';
import {View} from 'backbone';
import {select} from 'd3-selection';
import {axisLeft, axisBottom} from 'd3-axis';
import {event as currentEvent} from 'd3-selection'; 
export default class BaseChartView extends View {

    constructor(options) {
        super(options);
        this.options = options;
        this.firstRender = true;
    }

    onRender() {
        select(window).on('resize.' + this.cid, this.onAttach.bind(this));
    }

    onDestroy() {
        select(window).on('resize.' + this.cid, null);
    }

    onAttach() {
        this.firstRender = false;
    }

    setupViewport() {
        this.$el.html(`<svg class="chart__svg">
            <g class="chart__axis chart__axis_x"></g>
            <g class="chart__axis chart__axis_y"></g>
            <g class="chart__plot"></g>
        </svg>`);
        return select(this.$el[0]).select('svg');
    }

    makeLeftAxis(element, options, translate){
        const axis = axisLeft();
        return this.makeAxis(axis, element, options, translate);
    }

    makeBottomAxis(element, options, translate){
        const axis = axisBottom();
        return this.makeAxis(axis, element, options, translate);
    }

    makeAxis(axis, element, options, {left = 0, top = 0} = {}) {
        Object.keys(options).forEach(option => axis[option](options[option]));
        element.call(axis).attrs({
            transform: `translate(${left},${top})`
        });
        return axis;
    }

    getTooltipContent() {}

    onItemOver(d) {
        this.showTooltip(d, currentEvent.target);
    }

    showTooltip(d, element) {
        this.tooltip.show(this.getTooltipContent(d), this.$(element));
    }

    hideTooltip() {
        this.tooltip.hide();
    }

    bindTooltip(selection) {
        selection.on('mouseenter', this.onItemOver.bind(this))
            .on('mouseleave', this.hideTooltip.bind(this));
    }
}
