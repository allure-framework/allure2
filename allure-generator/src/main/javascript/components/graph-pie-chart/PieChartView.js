import { interpolate } from "d3-interpolate";
import { select } from "d3-selection";
import { arc, pie } from "d3-shape";
import { omit } from "underscore";
import BaseChartView from "../../components/graph-base/BaseChartView";
import TooltipView from "../../components/tooltip/TooltipView";
import { on } from "../../decorators";
import translate from "../../helpers/t";
import escape from "../../utils/escape";
import { values } from "../../utils/statuses";

const PADDING = 5;

class PieChartView extends BaseChartView {
  initialize(options) {
    this.options = options;
    this.model = this.options.model;
    this.showLegend = this.options ? this.options.showLegend || false : false;

    this.arc = arc();
    this.pie = pie()
      .sort(null)
      .value((d) => d.value);
    this.tooltip = new TooltipView({ position: "center" });
    this.getChartData();
  }

  getChartData() {
    this.statistic = this.model.get("statistic");
    const { total } = this.statistic;
    const stats = omit(this.statistic, "total");
    this.data = Object.keys(stats).map((key) => ({
      name: key.toUpperCase(),
      value: stats[key],
      part: stats[key] / total,
    })).filter(item => item.value);
  }

  setupViewport() {
    super.setupViewport();
    if (this.showLegend) {
      this.$el.append(this.getLegendTpl());
    }
    return this.svg;
  }

  onAttach() {
    const data = this.data;
    const width = this.$el.outerWidth();
    const height = this.$el.outerHeight();
    const radius = Math.min(width, height) / 2 - 2 * PADDING;
    const topOffset = height / 2;
    let leftOffset = width / 2;

    if (this.showLegend) {
      leftOffset -= 70;
    }
    this.arc.innerRadius(0.8 * radius).outerRadius(radius);

    this.svg = this.setupViewport();

    const sectors = this.svg
      .select(".chart__plot")
      .attrs({ transform: `translate(${leftOffset},${topOffset})` })
      .selectAll(".chart__arc")
      .data(this.pie(data))
      .enter()
      .append("path")
      .attr("class", (d) => `chart__arc chart__fill_status_${d.data.name.toLowerCase()}`);

    this.bindTooltip(sectors);

    this.svg
      .select(".chart__plot")
      .append("text")
      .classed("chart__caption", true)
      .attrs({ dy: "0.4em" })
      .styles({ "font-size": `${radius / 3}px` })
      .text(this.getChartTitle());

    if (this.firstRender) {
      sectors
        .transition()
        .duration(750)
        .attrTween("d", (d) => {
          const startAngleFn = interpolate(0, d.startAngle);
          const endAngleFn = interpolate(0, d.endAngle);
          return (t) => this.arc({ startAngle: startAngleFn(t), endAngle: endAngleFn(t) });
        });
    } else {
      sectors.attr("d", (d) => this.arc(d));
    }
    super.onAttach();
  }

  formatNumber(n) {
    return (Math.floor(n * 100) / 100).toString();
  }

  getChartTitle() {
    const { passed, total } = this.statistic;
    return `${this.formatNumber(((passed || 0) / total) * 100)}%`;
  }

  getTooltipContent({ data }) {
    const value = data.value || 0;
    const part = data.part || 0;
    const status = data.name.toLowerCase();
    const name = translate(`status.${status}`, {});
    return escape`
            ${value} tests (${this.formatNumber(part * 100)}%)<br>
            ${name}
        `;
  }

  getLegendTpl() {
    return `<div class="chart__legend">
    ${values
      .map(
        (status) =>
          `<div class="chart__legend-row" data-status="${status}">
<span class="chart__legend-icon chart__legend-icon_status_${status}"></span> ${translate(
            `status.${status}`,
          )}</div>`,
      )
      .join("")}
</div>`;
  }

  @on("mouseleave .chart__legend-row")
  onLegendOut() {
    this.hideTooltip();
  }

  @on("mouseenter .chart__legend-row")
  onLegendHover(e) {
    const el = this.$(e.currentTarget);
    const status = el.data("status");
    const sector = this.$(`.chart__fill_status_${status}`)[0];
    const data = select(sector).datum();
    this.showTooltip(data, sector);
  }
}

export default PieChartView;
