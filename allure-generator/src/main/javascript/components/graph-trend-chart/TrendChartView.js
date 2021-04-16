import "./styles.scss";
import { max } from "d3-array";
import { scaleLinear, scalePoint } from "d3-scale";
import { scaleOrdinal, schemeCategory20 } from "d3-scale";
import { area, line, stack } from "d3-shape";
import BaseChartView from "../../components/graph-base/BaseChartView";
import TooltipView from "../../components/tooltip/TooltipView";
import translate from "../../helpers/t";
import trendTooltip from "./trend-tooltip.hbs";

class TrendChartView extends BaseChartView {
  PAD_BOTTOM = 50;

  initialize(options) {
    this.x = scalePoint();
    this.y = scaleLinear();

    this.tooltip = new TooltipView({ position: "top" });
    this.keys = options.keys || this.model.keys();

    this.stack = stack()
      .keys(this.keys)
      .value((d, key) => d.data[key] || 0);

    this.color = options.colors || scaleOrdinal(schemeCategory20);

    options.notStacked && this.stack.offset(() => {});
    this.yTickFormat = options.yTickFormat || ((d) => d);
  }

  onAttach() {
    const data = this.model.toJSON();
    if (data && data.length > 1) {
      this.doShow(data);
    } else {
      this.$el.html(`<div class="widget__noitems">${translate("chart.trend.empty")}</div>`);
    }
    super.onAttach();
  }

  doShow(data) {
    this.setupViewport();
    this.x.range([0, this.width]);
    this.y.range([this.height, 0]);
    this.x.domain(data.map((d) => d.id));
    this.y.domain([0, max(data, (d) => d.total)]).nice();

    const trendStack = this.stack(data);
    this.makeBottomAxis({
      scale: this.x,
      tickFormat: (d, i) => data[i].name,
    });

    this.makeLeftAxis({
      scale: this.y,
      tickFormat: this.yTickFormat,
    });

    if (document.dir === "rtl") {
      this.svg
        .selectAll(".chart__axis_x")
        .selectAll("text")
        .style("text-anchor", "start");
    } else {
      this.svg
        .selectAll(".chart__axis_x")
        .selectAll("text")
        .style("text-anchor", "end");
    }

    this.svg
      .selectAll(".chart__axis_x")
      .selectAll("text")
      .attr("dx", "-.8em")
      .attr("dy", "-.6em")
      .attr("transform", "rotate(-90)");

    this.options.hideAreas || this.showAreas(trendStack);
    this.options.hideLines || this.showLines(trendStack);
    this.options.hidePoints || this.showPoints(trendStack);
    this.showSlices(data);
  }

  showAreas(trendStack) {
    const trendArea = area()
      .x((d) => this.x(d.data.id))
      .y0((d) => this.y(d[0]))
      .y1((d) => this.y(d[1]));

    this.plot
      .selectAll(".trend__area")
      .data(trendStack)
      .enter()
      .append("path")
      .attr("class", "trend__area")
      .attr("d", trendArea)
      .style("fill", (d) => this.color(d.key))
      .style("opacity", 0.85);
  }

  showLines(trendStack) {
    const trendLine = line()
      .x((d) => this.x(d.data.id))
      .y((d) => this.y(d[1]));

    this.plot
      .selectAll(".trend__line")
      .data(trendStack)
      .enter()
      .append("path")
      .attr("class", ".trend__line")
      .attr("d", trendLine)
      .style("stroke-width", 2)
      .style("stroke", (d) => this.color(d.key));
  }

  showPoints(trendStack) {
    const points = this.plot
      .selectAll(".trend_points")
      .data(trendStack)
      .enter()
      .append("g")
      .attr("class", ".trend_point")
      .style("fill", (d) => this.color(d.key));

    points
      .selectAll(".trend_point")
      .data((d) => d)
      .enter()
      .append("circle")
      .attr("r", 2)
      .attr("cx", (d) => this.x(d.data.id))
      .attr("cy", (d) => this.y(d[1]))
      .attr("class", "trend_point");
  }

  showSlices(data) {
    this.plot
      .selectAll(".slice")
      .data(data)
      .enter()
      .append("g")
      .attr("class", "slice");

    this.plot
      .selectAll(".slice")
      .filter((d) => d.reportUrl)
      .append("a")
      .attr("class", "edge")
      .filter((d) => d.reportUrl)
      .attr("xlink:href", (d) => d.reportUrl);

    this.plot
      .selectAll(".slice")
      .filter((d) => !d.reportUrl)
      .append("g")
      .attr("class", "edge");

    this.plot
      .selectAll(".edge")
      .append("line")
      .attr("id", (d) => d.id)
      .attr("x1", (d) => this.x(d.id))
      .attr("y1", (d) => this.y(d.total))
      .attr("x2", (d) => this.x(d.id))
      .attr("y2", this.y(0))
      .attr("stroke", "white")
      .attr("stroke-width", 1)
      .attr("class", "report-line");

    this.plot
      .selectAll(".edge")
      .append("rect")
      .style("opacity", 0.0)
      .attr("class", "report-edge")
      .attr("x", (d, i) => (i > 0 ? this.x(d.id) - this.x.step() / 2 : 0))
      .attr("y", 0)
      .attr("height", this.height)
      .attr("width", (d, i) =>
        i === 0 || this.x(d.id) === this.width ? this.x.step() / 2 : this.x.step(),
      )
      .on("mouseover", (d) => {
        const anchor = this.plot
          .append("circle")
          .attr("class", "anchor")
          .attr("cx", `${this.x(d.id)}`)
          .attr("cy", `${this.y(d.total / 2)}`);
        this.showTooltip(d, anchor.node());
      })
      .on("mouseout", () => {
        this.plot.selectAll(".anchor").remove();
        this.hideTooltip();
      });
  }

  getTooltipContent(selectedData) {
    const tooltipData = {
      ...selectedData,
      data: this.keys
        .map((key) => {
          return {
            key,
            num: this.yTickFormat(selectedData.data[key]),
            color: this.color(key),
          };
        })
        .filter((item) => !!item.num)
        .reverse(),
    };
    return trendTooltip(tooltipData);
  }
}

export default TrendChartView;
