import { histogram, max } from "d3-array";
import { scaleLinear, scaleSqrt } from "d3-scale";
import BaseChartView from "../../components/graph-base/BaseChartView";
import PopoverView from "../../components/popover/PopoverView";
import duration from "../../helpers/duration";
import translate from "../../helpers/t";
import escape from "../../utils/escape";

export default class DurationChart extends BaseChartView {
  initialize() {
    this.collection = this.model;
    this.getChartData();
  }

  getChartData() {
    this.data = this.collection
      .map((testResult) => ({
        value: testResult.time.duration,
        name: testResult.name,
      }))
      .filter((testResult) => {
        return testResult.value !== null;
      });
  }

  onAttach() {
    if (this.data && this.data.length) {
      this.doShow();
    } else {
      this.$el.html(`<div class="widget__noitems">${translate("chart.duration.empty")}</div>`);
    }
    super.onAttach();
  }

  doShow() {
    this.x = scaleLinear();
    this.y = scaleSqrt();
    this.tooltip = new PopoverView({ position: "right" });

    this.setupViewport();

    this.x.range([0, this.width]);
    this.y.range([this.height, 0], 1);

    const maxDuration = max(this.data, (d) => d.value);
    this.x.domain([0, Math.max(maxDuration, 10)]).nice();

    const bins = histogram()
      .value((d) => d.value)
      .domain(this.x.domain())
      .thresholds(this.x.ticks())(this.data)
      .map((bin) => ({
        x0: bin.x0,
        x1: bin.x1,
        y: bin.length,
        testResults: bin,
      }));

    const maxY = max(bins, (d) => d.y);
    this.y.domain([0, maxY]).nice();

    this.makeBottomAxis({
      scale: this.x,
      tickFormat: (time) => duration(time, 1),
    });

    this.makeLeftAxis({
      scale: this.y,
      ticks: Math.min(10, maxY),
    });

    let bars = this.plot
      .selectAll(".chart__bar")
      .data(bins)
      .enter()
      .append("rect")
      .classed("chart__bar", true);

    this.bindTooltip(bars);

    bars.attrs({
      x: (d) => this.x(d.x0) + 2,
      y: this.height,
      width: (d) => Math.max(this.x(d.x1) - this.x(d.x0) - 2, 0),
      height: 0,
    });

    if (this.firstRender) {
      bars = bars.transition().duration(500);
    }

    bars.attrs({
      y: (d) => this.y(d.y),
      height: (d) => this.height - this.y(d.y),
    });
  }

  getTooltipContent({ testResults }) {
    const LIST_LIMIT = 10;
    const items = testResults.slice(0, LIST_LIMIT);
    const overLimit = testResults.length - items.length;
    return `<b>${testResults.length} test cases</b><br>
            <ul class="popover__list">
                ${items.map((testResult) => escape`<li>${testResult.name}</li>`).join("")}
            </ul>
            ${overLimit ? `...and ${overLimit} more` : ""}
        `;
  }
}
