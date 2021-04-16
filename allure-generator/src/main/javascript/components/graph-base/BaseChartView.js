import "./styles.scss";
import { View } from "backbone";
import { axisBottom, axisLeft } from "d3-axis";
import { event as currentEvent, select } from "d3-selection";
import template from "./BaseChartView.hbs";

export default class BaseChartView extends View {
  PAD_LEFT = 50;
  PAD_RIGHT = 15;
  PAD_TOP = 10;
  PAD_BOTTOM = 30;

  constructor(options) {
    super(options);
    this.options = options;
    this.firstRender = true;
  }

  onRender() {
    select(window).on(`resize.${this.cid}`, this.onAttach.bind(this));
  }

  onDestroy() {
    select(window).on(`resize.${this.cid}`, null);
  }

  onAttach() {
    this.firstRender = false;
  }

  setupViewport() {
    this.width = Math.floor(this.$el.outerWidth()) - this.PAD_LEFT - this.PAD_RIGHT;
    this.height = Math.floor(this.$el.outerHeight()) - this.PAD_BOTTOM - this.PAD_TOP;
    this.$el.html(template(this));
    this.svg = select(this.$el[0]).select(".chart__svg");
    this.plot = this.svg.select(".chart__plot");
  }

  makeLeftAxis(options) {
    const axis = axisLeft();
    return this.makeAxis(axis, this.svg.select(".chart__axis_y"), options, {
      left: this.PAD_LEFT,
      top: this.PAD_TOP,
    });
  }

  makeBottomAxis(options) {
    const axis = axisBottom();
    return this.makeAxis(axis, this.svg.select(".chart__axis_x"), options, {
      left: this.PAD_LEFT,
      top: this.PAD_TOP + this.height,
    });
  }

  makeAxis(axis, element, options, { left = 0, top = 0 } = {}) {
    Object.keys(options).forEach((option) => axis[option](options[option]));
    element.call(axis).attrs({
      transform: `translate(${left},${top})`,
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
    selection
      .on("mouseenter", this.onItemOver.bind(this))
      .on("mouseleave", this.hideTooltip.bind(this));
  }
}
