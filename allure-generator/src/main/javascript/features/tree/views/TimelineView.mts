import "./TimelineView.scss";
import { axisBottom } from "d3-axis";
import { brushX } from "d3-brush";
import { drag } from "d3-drag";
import { scaleLinear } from "d3-scale";
import { select } from "d3-selection";
import duration from "../../../helpers/duration.mts";
import translate from "../../../helpers/t.mts";
import { createElement, createFragment } from "../../../shared/dom.mts";
import BaseChartView from "../../../shared/ui/BaseChartView.mts";
import TooltipView from "../../../shared/ui/TooltipView.mts";
import getComparator from "../model/comparator.mts";
import { byDuration } from "../model/filter.mts";
import { projectTreeData } from "../model/treeData.mts";
import { createTimelineViewContent } from "./renderTimelineView.mts";

const BRUSH_HEIGHT = 20;
const BRUSH_VIEW_HEIGHT = 48;
const BAR_HEIGHT = 20;
const BAR_GAP = 2;
const PADDING = 30;

type TimelineTreeNode = import("../../../types/report.mts").TreeNode;
type LoadedTreeData = import("../model/treeData.mts").LoadedTreeData;
type TimelineOptions = {
  treeData: LoadedTreeData;
};

const getTimelineResultLinkLabel = (result: TimelineTreeNode) => {
  const name = result.name || result.uid || translate("component.tree.unknown");
  const status = translate(`status.${result.status || "unknown"}`);

  return translate("tab.timeline.resultLink", { hash: { name, status } });
};

class TimelineView extends BaseChartView {
  rootClassName = "timeline";

  declare treeData: LoadedTreeData;

  declare chartX: import("d3-scale").ScaleLinear<number, number>;

  declare brushX: import("d3-scale").ScaleLinear<number, number>;

  declare sorter: import("../model/treeData.mts").TreeSorter;

  declare brush: import("d3-brush").BrushBehavior<unknown>;

  declare treeProjection: LoadedTreeData;

  declare minDuration: number;

  declare maxDuration: number;

  declare selectedDuration: number;

  declare data: TimelineTreeNode[];

  declare total: number;

  declare timeOffset: (durationValue: number) => number;

  declare svgChart: import("d3-selection").Selection<SVGSVGElement, unknown, null, undefined>;

  declare svgBrush: import("d3-selection").Selection<SVGSVGElement, unknown, null, undefined>;

  declare slider: import("d3-selection").Selection<SVGGElement, unknown, null, undefined>;

  declare handle: import("d3-selection").Selection<SVGCircleElement, unknown, null, undefined>;

  declare xChartAxis: import("d3-axis").Axis<import("d3-axis").AxisDomain>;

  declare xBrushAxis: import("d3-axis").Axis<import("d3-axis").AxisDomain>;

  constructor(options: TimelineOptions) {
    super(options);
    this.treeData = options.treeData;
    this.chartX = scaleLinear();
    this.brushX = scaleLinear();
    this.sorter = getComparator({
      sorter: "sorter.name",
      ascending: true,
    });
    this.brush = brushX().on("start brush end", this.onBrushChange.bind(this));
    this.tooltip = new TooltipView({
      position: "bottom",
    });
    this.treeProjection = projectTreeData(this.treeData, () => true, this.sorter);
    this.minDuration = this.treeData.time.minDuration || 0;
    this.maxDuration = this.treeData.time.maxDuration || this.minDuration;
    this.selectedDuration = this.minDuration;
    this.data = this.treeProjection.items;
    this.total = this.treeData.allResults.length;
    this.timeOffset = (durationValue) => durationValue - (this.treeData.time.start || 0);
  }

  drawChart() {
    this.doShow();
  }

  setupViewport() {
    this.el.replaceChildren(
      createTimelineViewContent({
        BRUSH_VIEW_HEIGHT,
        PADDING,
      }),
    );
    this.svgChart = select(this.el).select(".timeline__chart_svg");
    this.svgBrush = select(this.el).select(".timeline__brush_svg");
    this.slider = this.svgChart.select(".timeline__slider");
  }

  setupSlider() {
    const sliderScale = scaleLinear()
      .range([0, this.width])
      .domain([this.minDuration, this.maxDuration])
      .clamp(true);
    this.slider
      .append("line")
      .attr("class", "timeline__slider_track")
      .attr("x1", sliderScale.range()[0])
      .attr("x2", sliderScale.range()[1]);
    this.handle = this.slider
      .insert("circle")
      .attr("class", "timeline__slider_handle")
      .attr("cx", sliderScale(this.selectedDuration))
      .attr("r", 8)
      .call(
        drag<SVGCircleElement, unknown>()
          .on("drag", (event) => {
            this.selectedDuration = sliderScale.invert(event.x);
            this.handle.attr("cx", sliderScale(this.selectedDuration));
          })
          .on("end", () => {
            const filter = byDuration(this.selectedDuration, this.maxDuration);
            this.treeProjection = projectTreeData(this.treeData, filter, this.sorter);
            this.data = this.treeProjection.items;
            this.doShow();
            this.handle.attr("cx", sliderScale(this.selectedDuration));
          }),
      );
    const selectedResults = this.treeProjection.testResults.length;
    const opts = {
      count: selectedResults,
      percent: ((100 * selectedResults) / this.total).toFixed(2),
      duration: duration(this.selectedDuration),
    };
    this.slider
      .insert("g")
      .append("text")
      .attr("transform", `translate(${this.width / 2}, 20)`)
      .attr("class", "timeline__slider_text")
      .text(
        translate("tab.timeline.selected", {
          hash: opts,
        }),
      );
    this.slider
      .insert("g")
      .attr("class", "timeline__slider_text")
      .attr("transform", `translate(0, ${20})`)
      .selectAll("text")
      .data(sliderScale.domain())
      .enter()
      .append("text")
      .attr("x", sliderScale)
      .text((d) => duration(d, 1));
  }

  doShow() {
    const { width: elementWidth } = this.el.getBoundingClientRect();
    this.width = elementWidth > 2 * PADDING ? elementWidth - 2 * PADDING : elementWidth;
    const domain = [this.treeData.time.start || 0, this.treeData.time.stop || 0] as [
      number,
      number,
    ];
    this.chartX.domain(domain).range([0, this.width]);
    this.brushX.domain(domain).range([0, this.width]);
    this.setupViewport();
    this.setupSlider();
    let height = 10;
    const group = this.svgChart.select<SVGGElement>(".timeline__plot");
    height += this.drawTestGroups(this.data, height, group, true);
    this.xChartAxis = this.makeAxis(
      axisBottom(this.chartX) as unknown as import("d3-axis").Axis<import("d3-axis").AxisDomain>,
      this.svgChart.select(".timeline__chart__axis_x"),
      {
        scale: this.chartX as unknown as import("d3-axis").AxisScale<import("d3-axis").AxisDomain>,
        tickFormat: () => "",
        tickSizeOuter: 0,
        tickSizeInner: height,
      },
    );
    this.xBrushAxis = this.makeAxis(
      axisBottom(this.chartX) as unknown as import("d3-axis").Axis<import("d3-axis").AxisDomain>,
      this.svgBrush.select(".timeline__brush__axis_x"),
      {
        scale: this.chartX as unknown as import("d3-axis").AxisScale<import("d3-axis").AxisDomain>,
        tickFormat: (d: import("d3-scale").NumberValue) =>
          duration(Number(d) - (this.treeData.time.start || 0), 2),
        tickSizeOuter: 0,
      },
      {
        top: BAR_GAP + BRUSH_HEIGHT + BAR_GAP,
        left: PADDING,
      },
    );
    this.brush.extent([
      [0, 0],
      [this.width, BRUSH_HEIGHT],
    ]);
    this.svgBrush
      .append("g")
      .attr("transform", `translate(${PADDING}, ${BAR_GAP})`)
      .attr("class", "brush")
      .call(this.brush)
      .call(this.brush.move, this.chartX.range() as [number, number]);
    this.svgChart.attr("height", () => {
      return PADDING + height;
    });
  }

  drawTestGroups(
    items: TimelineTreeNode[],
    offset: number,
    parent: import("d3-selection").Selection<SVGGElement, unknown, null, undefined>,
    showTitle: boolean,
  ) {
    items
      .filter((item) => item.children)
      .forEach((item) => {
        let groupHeight = 0;
        const group = parent
          .append("g")
          .attr("class", "timeline__group")
          .attr("transform", `translate(0, ${offset})`);
        if (showTitle) {
          const text = group
            .append("text")
            .datum(item)
            .text((d: TimelineTreeNode) => d.name || "")
            .attr("class", "timeline__group_title");
          this.bindTooltip(text);
          groupHeight = BAR_HEIGHT + BAR_GAP;
          offset += groupHeight;
        }
        offset += this.drawTestGroups(item.children || [], groupHeight, group, false);
      });
    offset += this.drawTestResults(
      items.filter((item) => !item.children),
      parent,
    );
    return offset;
  }

  drawTestResults(
    items: TimelineTreeNode[],
    parent: import("d3-selection").Selection<SVGGElement, unknown, null, undefined>,
  ) {
    if (items.length) {
      const bars = parent
        .selectAll(".timeline__item")
        .data(items)
        .enter()
        .append("a")
        .attr("xlink:href", (d: TimelineTreeNode) => `#testresult/${d.uid}`)
        .attr("aria-label", getTimelineResultLinkLabel)
        .append("rect")
        .attr(
          "class",
          (d: TimelineTreeNode) => `timeline__item timeline__item_status_${d.status || "unknown"}`,
        )
        .attr("x", (d: TimelineTreeNode) => this.chartX(d.time?.start || 0))
        .attr("width", (d: TimelineTreeNode) =>
          this.chartX((d.time?.start || 0) + (d.time?.duration || 0)),
        )
        .attr("rx", 2)
        .attr("ry", 2)
        .attr("height", BAR_HEIGHT);
      this.bindTooltip(bars);
      bars.on("click", this.hideTooltip.bind(this));
      return BAR_HEIGHT + BAR_GAP;
    }
    return 0;
  }

  onBrushChange(event: import("d3-brush").D3BrushEvent<unknown>) {
    const selection = event.selection;
    const start = (d: TimelineTreeNode) =>
      Math.max(0, Math.min(this.chartX(d.time?.start || 0), this.width));
    const stop = (d: TimelineTreeNode) =>
      Math.max(0, Math.min(this.chartX(d.time?.stop || 0), this.width));
    if (selection) {
      const brushRange = selection as [number, number];
      this.chartX.domain(brushRange.map((value) => this.brushX.invert(value)) as [number, number]);
      this.svgChart
        .selectAll<SVGRectElement, TimelineTreeNode>(".timeline__item")
        .attr("x", (d) => start(d))
        .attr("width", (d) => stop(d) - start(d));
      this.svgBrush
        .select<SVGGElement>(".timeline__brush__axis_x")
        .call(
          this.xBrushAxis as unknown as (
            selection: import("d3-selection").Selection<SVGGElement, unknown, null, undefined>,
          ) => void,
        );
      this.svgChart
        .select<SVGGElement>(".timeline__chart__axis_x")
        .call(
          this.xChartAxis as unknown as (
            selection: import("d3-selection").Selection<SVGGElement, unknown, null, undefined>,
          ) => void,
        );
    }
    this.svgBrush.selectAll(".handle").attr("y", 0).attr("height", BAR_HEIGHT);
  }

  getTooltipContent(d: TimelineTreeNode) {
    const start = d.time?.start || 0;
    const stop = d.time?.stop || start;
    return createFragment(
      d.name || "",
      createElement("br"),
      `${duration(this.timeOffset(start))} \u2014 ${duration(this.timeOffset(stop))}`,
    );
  }
}

export default TimelineView;
