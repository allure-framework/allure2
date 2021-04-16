import { max } from "d3-array";
import { scaleBand, scaleSqrt } from "d3-scale";
import BaseChartView from "../../components/graph-base/BaseChartView";
import PopoverView from "../../components/popover/PopoverView";
import escape from "../../utils/escape";
import { values } from "../../utils/statuses";

const severities = ["blocker", "critical", "normal", "minor", "trivial"];

export default class SeverityChartView extends BaseChartView {
  initialize() {
    this.x = scaleBand().domain(severities);
    this.y = scaleSqrt();
    this.status = scaleBand().domain(values);
    this.tooltip = new PopoverView({ position: "right" });
    this.collection = this.model;
    this.getChartData();
  }

  getChartData() {
    this.data = severities.map((severity) =>
      values.map((status) => {
        const testResults = this.collection.filter(function(item) {
          return item.status === status && item.severity === severity;
        });
        return {
          value: testResults.length,
          testResults,
          severity,
          status,
        };
      }),
    );
  }

  onAttach() {
    const data = this.data;
    this.setupViewport();

    this.x.range([0, this.width]);
    this.y.range([this.height, 0], 1);
    this.y.domain([0, max(data, (d) => max(d, (dd) => dd.value))]).nice();
    this.status.rangeRound([0, this.x.step()]);

    this.makeBottomAxis({
      tickFormat: (d) => d.toLowerCase(),
      scale: this.x,
    });

    this.svg
      .selectAll(".tick")
      .select("line")
      .attr("transform", `translate(${this.x.step() / 2} , 0)`);

    this.makeLeftAxis({
      scale: this.y,
      ticks: Math.min(10, this.y.domain()[1]),
    });

    let bars = this.svg
      .select(".chart__plot")
      .selectAll(".chart__group")
      .data(data)
      .enter()
      .append("g")
      .attr("transform", (d) => `translate(${this.x(d[0].severity)},0)`)
      .selectAll(".bar")
      .data((d) => d)
      .enter()
      .append("rect");

    bars.attrs({
      x: (d) => this.status(d.status),
      y: this.height,
      height: 0,
      width: this.status.step(),
      class: (d) => `chart__bar chart__fill_status_${d.status}`,
    });

    this.bindTooltip(bars);

    if (this.firstRender) {
      bars = bars.transition().duration(500);
    }

    bars.attrs({
      y: (d) => this.y(d.value),
      height: (d) => this.height - this.y(d.value),
    });
    super.onAttach();
  }

  getTooltipContent({ value, severity, status, testResults }) {
    const LIST_LIMIT = 10;
    const items = testResults.slice(0, LIST_LIMIT);
    const overLimit = testResults.length - items.length;
    return `<b>${value} ${severity.toLowerCase()} test cases ${status.toLowerCase()}</b><br>
            <ul class="popover__list">
                ${items.map((testResult) => escape`<li>${testResult.name}</li>`).join("")}
            </ul>
            ${overLimit ? `...and ${overLimit} more` : ""}
        `;
  }
}
