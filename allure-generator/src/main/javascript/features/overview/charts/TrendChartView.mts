import "./TrendChartView.scss";
import { max } from "d3-array";
import { scaleLinear, scalePoint } from "d3-scale";
import { scaleOrdinal } from "d3-scale";
import { schemeCategory10 } from "d3-scale-chromatic";
import { area, line, stack } from "d3-shape";
import translate from "../../../helpers/t.mts";
import { createElement, createFragment } from "../../../shared/dom.mts";
import BaseChartView from "../../../shared/ui/BaseChartView.mts";
import TooltipView from "../../../shared/ui/TooltipView.mts";

type Series<Datum, Key> = import("d3-shape").Series<Datum, Key>;
type SeriesPoint<Datum> = import("d3-shape").SeriesPoint<Datum>;
type TrendPoint = import("../../../types/report.mts").TrendPoint;
type TrendChartOptions = {
  items?: TrendPoint[];
  keys?: string[];
  colors?: (key: string) => string;
  notStacked?: boolean;
  yTickFormat?: (value: number) => string | number;
  hideAreas?: boolean;
  hideLines?: boolean;
  hidePoints?: boolean;
};

const getTrendReportLinkLabel = (point: TrendPoint) =>
  translate("chart.trend.reportLink", { hash: { name: point.name } });

class TrendChartView extends BaseChartView {
  PAD_BOTTOM = 50;

  declare x: import("d3-scale").ScalePoint<number>;

  declare y: import("d3-scale").ScaleLinear<number, number>;

  declare items: TrendPoint[];

  declare keys: string[];

  declare stack: ReturnType<typeof stack<TrendPoint, string>>;

  declare color: (key: string) => string;

  declare yTickFormat: (value: number) => string | number;

  constructor(options: TrendChartOptions = {}) {
    super(options);
    this.x = scalePoint<number>();
    this.y = scaleLinear();

    this.tooltip = new TooltipView({ position: "top" });
    this.items = (options.items || []) as TrendPoint[];
    this.keys =
      options.keys ||
      Array.from(new Set(this.items.flatMap((item) => Object.keys(item.data || {}))));

    this.stack = stack<TrendPoint, string>()
      .keys(this.keys)
      .value((d, key) => d.data[key] || 0);

    this.color = options.colors || scaleOrdinal(schemeCategory10);

    if (options.notStacked) {
      this.stack.offset(() => {});
    }
    this.yTickFormat = options.yTickFormat || ((d) => d);
  }

  drawChart() {
    const data = this.items;
    if (data && data.length > 1) {
      this.doShow(data);
    } else {
      this.el.replaceChildren(
        createElement("div", {
          className: "widget__noitems",
          text: translate("chart.trend.empty"),
        }),
      );
    }
  }

  doShow(data: TrendPoint[]) {
    this.setupViewport();
    this.x.range([0, this.width]);
    this.y.range([this.height, 0]);
    this.x.domain(data.map((d) => d.id));
    this.y.domain([0, max(data, (d) => d.total) || 0]).nice();

    const trendStack = this.stack(data);
    this.makeBottomAxis({
      scale: this.x,
      tickFormat: (_d: number, i: number) => data[i]?.name || "",
    });

    this.makeLeftAxis({
      scale: this.y,
      tickFormat: this.yTickFormat,
    });

    if (document.dir === "rtl") {
      this.svg.selectAll(".chart__axis_x").selectAll("text").style("text-anchor", "start");
    } else {
      this.svg.selectAll(".chart__axis_x").selectAll("text").style("text-anchor", "end");
    }

    this.svg
      .selectAll(".chart__axis_x")
      .selectAll("text")
      .attr("dx", "-.8em")
      .attr("dy", "-.6em")
      .attr("transform", "rotate(-90)");

    if (!this.options.hideAreas) {
      this.showAreas(trendStack);
    }

    if (!this.options.hideLines) {
      this.showLines(trendStack);
    }

    if (!this.options.hidePoints) {
      this.showPoints(trendStack);
    }

    this.showSlices(data);
  }

  showAreas(trendStack: Series<TrendPoint, string>[]) {
    const trendArea = area<SeriesPoint<TrendPoint>>()
      .x((d) => this.x(d.data.id) ?? 0)
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

  showLines(trendStack: Series<TrendPoint, string>[]) {
    const trendLine = line<SeriesPoint<TrendPoint>>()
      .x((d) => this.x(d.data.id) ?? 0)
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

  showPoints(trendStack: Series<TrendPoint, string>[]) {
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
      .attr("cx", (d) => this.x(d.data.id) ?? 0)
      .attr("cy", (d) => this.y(d[1]))
      .attr("class", "trend_point");
  }

  showSlices(data: TrendPoint[]) {
    const slices = this.plot
      .selectAll<SVGGElement, TrendPoint>(".slice")
      .data(data)
      .enter()
      .append("g")
      .attr("class", "slice");

    slices
      .filter((d) => Boolean(d.reportUrl))
      .append("a")
      .attr("class", "edge")
      .filter((d) => Boolean(d.reportUrl))
      .attr("xlink:href", (d) => (typeof d.reportUrl === "string" ? d.reportUrl : ""))
      .attr("aria-label", getTrendReportLinkLabel);

    slices
      .filter((d) => !d.reportUrl)
      .append("g")
      .attr("class", "edge");

    const edges = this.plot.selectAll<SVGGElement, TrendPoint>(".edge");

    edges
      .append("line")
      .attr("id", (d) => d.id)
      .attr("x1", (d) => this.x(d.id) ?? 0)
      .attr("y1", (d) => this.y(d.total))
      .attr("x2", (d) => this.x(d.id) ?? 0)
      .attr("y2", this.y(0))
      .attr("stroke", "white")
      .attr("stroke-width", 1)
      .attr("class", "report-line");

    edges
      .append("rect")
      .style("opacity", 0.0)
      .attr("class", "report-edge")
      .attr("x", (d, i) => (i > 0 ? (this.x(d.id) ?? 0) - this.x.step() / 2 : 0))
      .attr("y", 0)
      .attr("height", this.height)
      .attr("width", (d, i) =>
        i === 0 || (this.x(d.id) ?? 0) === this.width ? this.x.step() / 2 : this.x.step(),
      )
      .on("mouseover", (event: MouseEvent, d: TrendPoint) => {
        const anchor = this.plot
          .append("circle")
          .attr("class", "anchor")
          .attr("cx", `${this.x(d.id) ?? 0}`)
          .attr("cy", `${this.y(d.total / 2)}`);
        const anchorElement = anchor.node();
        if (anchorElement) {
          this.showTooltip(d, anchorElement);
        }
      })
      .on("mouseout", () => {
        this.plot.selectAll(".anchor").remove();
        this.hideTooltip();
      });
  }

  getTooltipContent(selectedData: TrendPoint) {
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
    return createFragment(
      tooltipData.name,
      createElement("div", {
        className: "trend__tooltip",
        children: tooltipData.data.map(({ color, num, key }) =>
          createElement("div", {
            className: "trend__tooltip_label",
            children: [
              createElement("span", {
                attrs: {
                  style: `background-color: ${color}`,
                },
                className: "label",
                text: num,
              }),
              createElement("span", {
                className: "trend__tooltip_name",
                text: key,
              }),
            ],
          }),
        ),
      }),
    );
  }
}

export default TrendChartView;
