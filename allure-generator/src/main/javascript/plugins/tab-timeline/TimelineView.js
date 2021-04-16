import "./styles.scss";
import { axisBottom } from "d3-axis";
import { brushX } from "d3-brush";
import { drag } from "d3-drag";
import { scaleLinear } from "d3-scale";
import { event as currentEvent, select } from "d3-selection";
import BaseChartView from "../../components/graph-base/BaseChartView";
import TooltipView from "../../components/tooltip/TooltipView";
import getComparator from "../../data/tree/comparator";
import { byDuration } from "../../data/tree/filter";
import { className } from "../../decorators";
import duration from "../../helpers/duration";
import translate from "../../helpers/t";
import "d3-selection-multi";
import escape from "../../utils/escape";
import template from "./TimelineView.hbs";

const BRUSH_HEIGHT = 20;
const BAR_HEIGHT = 20;
const BAR_GAP = 2;
const PADDING = 30;

@className("timeline")
class TimelineView extends BaseChartView {
  initialize() {
    this.chartX = scaleLinear();
    this.brushX = scaleLinear();
    this.sorter = getComparator({ sorter: "sorter.name", ascending: true });

    this.brush = brushX().on("start brush end", this.onBrushChange.bind(this));
    this.tooltip = new TooltipView({ position: "bottom" });
    this.collection.applyFilterAndSorting(() => 1, this.sorter);
    this.minDuration = this.collection.time.minDuration;
    this.maxDuration = this.collection.time.maxDuration;
    this.selectedDuration = this.minDuration;
    this.data = this.collection.toJSON();
    this.total = this.collection.allResults.length;
    this.timeOffset = (d) => d - this.collection.time.start;
  }

  onAttach() {
    this.doShow();
  }

  setupViewport() {
    this.$el.html(template({ PADDING }));
    this.svgChart = select(this.$el[0]).select(".timeline__chart_svg");
    this.svgBrush = select(this.$el[0]).select(".timeline__brush_svg");
    this.slider = this.svgChart.select(".timeline__slider");
  }

  setupSlider() {
    const sliderScale = scaleLinear()
      .range([0, this.width])
      .domain([this.minDuration, this.maxDuration])
      .clamp(true);

    this.slider.append("line").attrs({
      class: "timeline__slider_track",
      x1: sliderScale.range()[0],
      x2: sliderScale.range()[1],
    });

    this.handle = this.slider
      .insert("circle")
      .attrs({
        class: "timeline__slider_handle",
        cx: sliderScale(this.selectedDuration),
        r: 8,
      })
      .call(
        drag()
          .on("drag", () => {
            this.selectedDuration = sliderScale.invert(currentEvent.x);
            this.handle.attr("cx", sliderScale(this.selectedDuration));
          })
          .on("end", () => {
            const filter = byDuration(this.selectedDuration, this.maxDuration);
            this.collection.applyFilterAndSorting(filter, this.sorter);
            this.data = this.collection.toJSON();
            this.doShow();
            this.handle.attr("cx", sliderScale(this.selectedDuration));
          }),
      );

    const selectedResults = this.collection.testResults.length;
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
      .text(translate("tab.timeline.selected", { hash: opts }));

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
    this.width = this.$el.width() > 2 * PADDING ? this.$el.width() - 2 * PADDING : this.$el.width();

    const domain = [this.collection.time.start, this.collection.time.stop];
    this.chartX.domain(domain).range([0, this.width]);
    this.brushX.domain(domain).range([0, this.width]);

    this.setupViewport();
    this.setupSlider();

    let height = 10;
    const group = this.svgChart.select(".timeline__plot");
    height += this.drawTestGroups(this.data, height, group, true);

    select(this.$el[0])
      .select(".timeline__brush")
      .style("top", () => {
        return `${Math.min(this.$el.height() - BRUSH_HEIGHT, height + PADDING)}px`;
      });

    this.xChartAxis = this.makeAxis(
      axisBottom(),
      this.svgChart.select(".timeline__chart__axis_x"),
      {
        scale: this.chartX,
        tickFormat: () => "",
        tickSizeOuter: 0,
        tickSizeInner: height,
      },
    );

    this.xBrushAxis = this.makeAxis(
      axisBottom(),
      this.svgBrush.select(".timeline__brush__axis_x"),
      {
        scale: this.chartX,
        tickFormat: (d) => duration(d - this.collection.time.start, 2),
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
      .attrs({
        transform: `translate(${PADDING}, ${BAR_GAP})`,
        class: "brush",
      })
      .call(this.brush)
      .call(this.brush.move, this.chartX.range());

    if (this.firstRender) {
      this.svgBrush
        .select(".brush")
        .transition()
        .duration(300)
        .call(this.brush.move, [(1 / 16) * this.width, (15 / 16) * this.width])
        .transition()
        .duration(500)
        .call(this.brush.move, this.chartX.range());
    }

    this.svgChart.attr("height", () => {
      return PADDING + height + BRUSH_HEIGHT;
    });

    super.onRender();
  }

  drawTestGroups(items, offset, parent, showTitle) {
    items
      .filter((item) => item.children)
      .forEach((item) => {
        let groupHeight = 0;
        const group = parent.append("g").attrs({
          class: "timeline__group",
          transform: `translate(0, ${offset})`,
        });

        if (showTitle) {
          const text = group
            .append("text")
            .datum(item)
            .text((d) => d.name)
            .attr("class", "timeline__group_title");
          this.bindTooltip(text);
          groupHeight = BAR_HEIGHT + BAR_GAP;
          offset += groupHeight;
        }

        offset += this.drawTestGroups(item.children, groupHeight, group, false);
      });
    offset += this.drawTestResults(
      items.filter((item) => !item.children),
      parent,
      offset,
    );
    return offset;
  }

  drawTestResults(items, parent) {
    if (items.length) {
      const bars = parent
        .selectAll(".timeline__item")
        .data(items)
        .enter()
        .append("a")
        .attr("xlink:href", (d) => `#testresult/${d.uid}`)
        .append("rect")
        .attrs({
          class: (d) => `timeline__item chart__fill_status_${d.status}`,
          x: (d) => this.chartX(d.time.start),
          width: (d) => this.chartX(d.time.start + d.time.duration),
          rx: 2,
          ry: 2,
          height: BAR_HEIGHT,
        });
      this.bindTooltip(bars);
      bars.on("click", this.hideTooltip.bind(this));
      return BAR_HEIGHT + BAR_GAP;
    }
    return 0;
  }

  onBrushChange() {
    const selection = currentEvent.selection;
    const start = (d) => Math.max(0, Math.min(this.chartX(d.time.start), this.width));
    const stop = (d) => Math.max(0, Math.min(this.chartX(d.time.stop), this.width));

    if (selection) {
      this.chartX.domain(selection.map(this.brushX.invert, this.brushX));
      this.svgChart.selectAll(".timeline__item").attrs({
        x: (d) => start(d),
        width: (d) => stop(d) - start(d),
      });
      this.svgBrush.select(".timeline__brush__axis_x").call(this.xBrushAxis);
      this.svgChart.select(".timeline__chart__axis_x").call(this.xChartAxis);
    }

    this.svgBrush.selectAll(".handle").attrs({
      y: 0,
      height: BAR_HEIGHT,
    });
  }

  getTooltipContent(d) {
    return escape`${d.name}<br>
            ${duration(this.timeOffset(d.time.start))} — ${duration(this.timeOffset(d.time.stop))}`;
  }
}

export default TimelineView;
