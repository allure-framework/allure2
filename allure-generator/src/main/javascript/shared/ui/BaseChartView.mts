import "./styles/BaseChartView.scss";
import { axisBottom, axisLeft } from "d3-axis";
import type { AxisDomain, AxisScale } from "d3-axis";
import { select } from "d3-selection";
import type { BaseType, Selection } from "d3-selection";
import { uniqueId } from "../../core/runtime/ids.mts";
import { bindDelegatedEvents } from "../../core/view/domEvents.mts";
import { createSvgElement } from "../dom.mts";

type ChartSelection<TElement extends BaseType> = Selection<TElement, unknown, null, undefined>;

type TooltipController = {
  show?: (content: string | Node | undefined, anchor: Element) => void;
  hide?: () => void;
};

type AxisOptions<TDomain extends AxisDomain> = Record<string, unknown> & {
  scale: AxisScale<TDomain>;
};

type AxisOffset = {
  left?: number;
  top?: number;
};

export default class BaseChartView {
  PAD_LEFT = 50;
  PAD_RIGHT = 15;
  PAD_TOP = 10;
  PAD_BOTTOM = 30;

  cid: string;

  el: HTMLDivElement;

  get mountElement() {
    return this.el;
  }

  options: Record<string, unknown>;

  isAttached: boolean;

  firstRender: boolean;

  drawFrame: number | null;

  rootClassName = "";

  width = 0;

  height = 0;

  svg!: ChartSelection<SVGSVGElement>;

  plot!: ChartSelection<SVGGElement>;

  tooltip?: TooltipController;

  resizeObserver: ResizeObserver | null;

  private releaseEvents: () => void;

  constructor(options: Record<string, unknown> = {}) {
    this.cid = uniqueId("chart");
    this.el = document.createElement("div");
    this.options = options;
    this.isAttached = false;
    this.drawFrame = null;
    this.firstRender = true;
    this.resizeObserver = null;
    this.releaseEvents = () => {};
  }

  render() {
    this.el.className = this.rootClassName;
    return this;
  }

  private scheduleDraw() {
    if (this.drawFrame !== null) {
      return;
    }

    this.drawFrame = window.requestAnimationFrame(() => {
      this.drawFrame = null;
      if (!this.isAttached || !this.el.isConnected) {
        return;
      }

      this.drawIntoElement();
    });
  }

  private observeResize() {
    if (typeof ResizeObserver !== "function" || this.resizeObserver) {
      return;
    }

    this.resizeObserver = new ResizeObserver(() => this.scheduleDraw());
    this.resizeObserver.observe(this.el);
  }

  attachToDom() {
    if (this.isAttached) {
      return;
    }

    this.isAttached = true;
    select(window).on(`resize.${this.cid}`, () => this.scheduleDraw());
    this.observeResize();
    this.scheduleDraw();
  }

  detachFromDom() {
    if (this.drawFrame !== null) {
      window.cancelAnimationFrame(this.drawFrame);
      this.drawFrame = null;
    }

    this.resizeObserver?.disconnect();
    this.resizeObserver = null;
    select(window).on(`resize.${this.cid}`, null);

    if (!this.isAttached) {
      return;
    }

    this.isAttached = false;
    this.releaseEvents();
    this.releaseEvents = () => {};
  }

  destroy() {
    this.detachFromDom();
    this.tooltip?.hide?.();
    this.el.remove();
  }

  protected getDelegatedEvents(): import("../../core/view/domEvents.mts").DelegatedEvents {
    return {};
  }

  protected drawChart() {}

  protected drawIntoElement() {
    this.releaseEvents();
    this.releaseEvents = () => {};
    this.drawChart();
    const events = this.getDelegatedEvents();
    if (Object.keys(events).length) {
      this.releaseEvents = bindDelegatedEvents({
        root: this.el,
        events,
        context: this,
      });
    }
    this.firstRender = false;
  }

  setupViewport() {
    const { width, height } = this.el.getBoundingClientRect();
    this.width = Math.max(Math.floor(width) - this.PAD_LEFT - this.PAD_RIGHT, 0);
    this.height = Math.max(Math.floor(height) - this.PAD_BOTTOM - this.PAD_TOP, 0);
    this.el.replaceChildren(
      createSvgElement("svg", {
        className: "chart__svg",
        children: [
          createSvgElement("g", {
            attrs: { transform: `translate(${this.PAD_LEFT}, ${this.PAD_TOP})` },
            className: "chart__plot",
          }),
          createSvgElement("g", {
            className: "chart__axis chart__axis_x",
          }),
          createSvgElement("g", {
            className: "chart__axis chart__axis_y",
          }),
        ],
      }),
    );
    this.svg = select(this.el).select(".chart__svg");
    this.plot = this.svg.select(".chart__plot");
  }

  makeLeftAxis<TDomain extends AxisDomain>(options: AxisOptions<TDomain>) {
    const axis = axisLeft(options.scale as never);
    return this.makeAxis(
      axis as import("d3-axis").Axis<AxisDomain>,
      this.svg.select(".chart__axis_y"),
      options as unknown as AxisOptions<AxisDomain>,
      {
        left: this.PAD_LEFT,
        top: this.PAD_TOP,
      },
    );
  }

  makeBottomAxis<TDomain extends AxisDomain>(options: AxisOptions<TDomain>) {
    const axis = axisBottom(options.scale as never);
    return this.makeAxis(
      axis as import("d3-axis").Axis<AxisDomain>,
      this.svg.select(".chart__axis_x"),
      options as unknown as AxisOptions<AxisDomain>,
      {
        left: this.PAD_LEFT,
        top: this.PAD_TOP + this.height,
      },
    );
  }

  makeAxis(
    axis: import("d3-axis").Axis<AxisDomain>,
    element: ChartSelection<SVGGElement>,
    options: AxisOptions<AxisDomain>,
    { left = 0, top = 0 }: AxisOffset = {},
  ) {
    const axisMethods = axis as unknown as Record<string, (value: unknown) => unknown>;
    Object.keys(options).forEach((option) => {
      if (option !== "scale" && typeof axisMethods[option] === "function") {
        axisMethods[option](options[option]);
      }
    });
    element.call(axis).attr("transform", `translate(${left},${top})`);
    return axis;
  }

  getTooltipContent(..._args: unknown[]): string | Node | undefined {
    return undefined;
  }

  showTooltip(d: unknown, element: Element) {
    this.tooltip?.show?.(this.getTooltipContent(d), element);
  }

  hideTooltip() {
    this.tooltip?.hide?.();
  }

  bindTooltip<TElement extends BaseType, TDatum, TParent extends BaseType>(
    selection: Selection<TElement, TDatum, TParent, unknown>,
  ) {
    selection
      .on("mouseenter", (event: MouseEvent, datum: TDatum) => {
        if (event.target instanceof Element) {
          this.showTooltip(datum, event.target);
        }
      })
      .on("mouseleave", () => {
        this.hideTooltip();
      });
  }
}
