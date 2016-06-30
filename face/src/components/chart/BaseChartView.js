import './styles.css';
import {View} from 'backbone';
import d3 from 'd3';

export default class BaseChartView extends View {

    constructor(options) {
        super(options);
        this.options = options;
        this.firstRender = true;
    }

    onRender() {
        d3.select(window).on('resize.' + this.cid, this.onShow.bind(this));
    }

    onDestroy() {
        d3.select(window).on('resize.' + this.cid, null);
    }

    onShow() {
        this.firstRender = false;
    }

    setupViewport() {
        this.$el.html(`<svg class="chart__svg">
            <g class="chart__axis chart__axis_x"></g>
            <g class="chart__axis chart__axis_y"></g>
            <g class="chart__plot"></g>
        </svg>`);
        return d3.select(this.$el[0]).select('svg');
    }

    makeAxis(element, options, {left = 0, top = 0} = {}) {
        const axis = d3.svg.axis();
        Object.keys(options).forEach(option => axis[option](options[option]));
        element.call(axis).attr({
            transform: `translate(${left},${top})`
        });
        return axis;
    }

    getTooltipContent() {}

    onItemOver(d) {
        this.showTooltip(d, d3.event.target);
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
