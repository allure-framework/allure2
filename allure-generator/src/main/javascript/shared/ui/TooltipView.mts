import "./styles/TooltipView.scss";
import { bindDelegatedEvents } from "../../core/view/domEvents.mts";
import { createFragmentFromHtml } from "../dom.mts";
import bem from "../bem/index.mts";

type TooltipPosition =
  | "top"
  | "top-right"
  | "top-left"
  | "center"
  | "right"
  | "left"
  | "bottom"
  | "bottom-left"
  | "bottom-right";

type TooltipOptions = {
  position?: TooltipPosition;
  offset?: number;
};
type TooltipContent = string | Node | null | undefined;

type TooltipRect = Pick<DOMRect, "top" | "left" | "height" | "width">;
type TooltipSize = Pick<DOMRect, "height" | "width">;
type TooltipOffsets = {
  offset: number;
};
type PositionCalculator = (
  rect: TooltipRect,
  offsets: TooltipOffsets,
  tipSize: TooltipSize,
) => { top: number; left: number };

type TooltipViewportPosition = {
  left: number;
  top: number;
};

const VIEWPORT_MARGIN = 8;

const HORIZONTAL_MIRROR_POSITION: Partial<Record<TooltipPosition, TooltipPosition>> = {
  "bottom-left": "bottom-right",
  "bottom-right": "bottom-left",
  left: "right",
  right: "left",
  "top-left": "top-right",
  "top-right": "top-left",
};

const FALLBACK_POSITION_CANDIDATES: Record<TooltipPosition, TooltipPosition[]> = {
  bottom: ["bottom", "top"],
  "bottom-left": ["bottom-left", "bottom-right", "top-left", "top-right"],
  "bottom-right": ["bottom-right", "bottom-left", "top-right", "top-left"],
  center: ["center"],
  left: ["left", "right"],
  right: ["right", "left"],
  top: ["top", "bottom"],
  "top-left": ["top-left", "top-right", "bottom-left", "bottom-right"],
  "top-right": ["top-right", "top-left", "bottom-right", "bottom-left"],
};

const POSITION: Record<TooltipPosition, PositionCalculator> = {
  top: ({ top, left, width }, { offset }, tipSize) => ({
    top: top - tipSize.height - offset,
    left: left + width / 2 - tipSize.width / 2,
  }),
  "top-right": ({ top, left, width }, { offset }, tipSize) => ({
    top: top - tipSize.height - offset,
    left: left + width + offset,
  }),
  "top-left": ({ top, left }, { offset }, tipSize) => ({
    top: top - tipSize.height - offset,
    left: left - tipSize.width - offset,
  }),
  center: ({ top, left, height, width }, _offsets, tipSize) => ({
    top: top + height / 2,
    left: left + width / 2 - tipSize.width / 2,
  }),
  right: ({ top, left, height, width }, { offset }, tipSize) => ({
    top: top + height / 2 - tipSize.height / 2,
    left: left + width + offset,
  }),
  left: ({ top, left, height }, { offset }, tipSize) => ({
    top: top + height / 2 - tipSize.height / 2,
    left: left - offset - tipSize.width,
  }),
  bottom: ({ top, left, height, width }, { offset }, tipSize) => ({
    top: top + height + offset,
    left: left + width / 2 - tipSize.width / 2,
  }),
  "bottom-left": ({ top, left, height, width }, { offset }, tipSize) => ({
    top: top + height + offset,
    left: left + width - tipSize.width,
  }),
  "bottom-right": ({ top, left, height }, { offset }, _tipSize) => ({
    top: top + height + offset,
    left,
  }),
};

class TooltipView {
  static container = document.body;

  el: HTMLDivElement;

  content: TooltipContent;

  options: TooltipOptions & { offset: number };

  className = "tooltip";

  positionClassBase?: string;

  private activePosition: TooltipPosition;

  private releaseEvents: () => void;

  constructor(options: TooltipOptions = {}) {
    this.el = document.createElement("div");
    this.content = "";
    this.options = {
      offset: 10,
      ...options,
    };
    this.activePosition = options.position || "top";
    this.releaseEvents = () => {};
  }

  protected getDelegatedEvents(): import("../../core/view/domEvents.mts").DelegatedEvents {
    return {};
  }

  private clearEvents() {
    this.releaseEvents();
    this.releaseEvents = () => {};
  }

  private resolveClassName(position = this.activePosition) {
    const baseClassName = this.className || "tooltip";
    const positionClassBase =
      this.positionClassBase || baseClassName.split(/\s+/).pop() || "tooltip";
    return `${baseClassName} ${bem(positionClassBase, {
      position,
    })}`.trim();
  }

  private setActivePosition(position: TooltipPosition) {
    this.activePosition = position;
    this.el.className = this.resolveClassName(position);
  }

  protected render() {
    this.el.className = this.resolveClassName();
    if (typeof this.content === "string") {
      this.el.replaceChildren(
        ...(this.content ? [createFragmentFromHtml(this.content, this.el)] : []),
      );
    } else if (this.content) {
      this.el.replaceChildren(this.content);
    } else {
      this.el.replaceChildren();
    }
    this.clearEvents();
    const events = this.getDelegatedEvents();
    if (Object.keys(events).length) {
      this.releaseEvents = bindDelegatedEvents({
        root: this.el,
        events,
        context: this,
      });
    }
  }

  isVisible() {
    if (!this.el.isConnected) {
      return false;
    }

    const rect = this.el.getBoundingClientRect();
    const style = getComputedStyle(this.el);
    return (
      rect.width > 0 && rect.height > 0 && style.display !== "none" && style.visibility !== "hidden"
    );
  }

  setContent(content?: TooltipContent) {
    this.content = content || "";
  }

  private getDirection() {
    return document.documentElement.dir || document.body.dir || document.dir || "ltr";
  }

  private resolvePreferredPosition(position: TooltipPosition) {
    if (this.getDirection() !== "rtl") {
      return position;
    }

    return HORIZONTAL_MIRROR_POSITION[position] || position;
  }

  private getViewportRect() {
    return {
      bottom: window.innerHeight - VIEWPORT_MARGIN,
      left: VIEWPORT_MARGIN,
      right: window.innerWidth - VIEWPORT_MARGIN,
      top: VIEWPORT_MARGIN,
    };
  }

  private getPositionCoordinates(
    anchorRect: TooltipRect,
    tooltipRect: TooltipSize,
    position: TooltipPosition,
  ) {
    return POSITION[position](
      anchorRect,
      {
        offset: this.options.offset,
      },
      tooltipRect,
    );
  }

  private getOverflowScore({ left, top }: TooltipViewportPosition, tooltipRect: TooltipSize) {
    const viewportRect = this.getViewportRect();
    const right = left + tooltipRect.width;
    const bottom = top + tooltipRect.height;
    return (
      Math.max(viewportRect.left - left, 0) +
      Math.max(viewportRect.top - top, 0) +
      Math.max(right - viewportRect.right, 0) +
      Math.max(bottom - viewportRect.bottom, 0)
    );
  }

  private clampToViewport(
    { left, top }: TooltipViewportPosition,
    tooltipRect: TooltipSize,
  ): TooltipViewportPosition {
    const viewportRect = this.getViewportRect();
    const maxLeft = Math.max(viewportRect.left, viewportRect.right - tooltipRect.width);
    const maxTop = Math.max(viewportRect.top, viewportRect.bottom - tooltipRect.height);

    return {
      left: Math.min(Math.max(left, viewportRect.left), maxLeft),
      top: Math.min(Math.max(top, viewportRect.top), maxTop),
    };
  }

  private resolvePosition(
    anchorRect: TooltipRect,
    tooltipRect: TooltipSize,
    position: TooltipPosition,
  ) {
    const candidates = FALLBACK_POSITION_CANDIDATES[position] || [position];
    let bestCandidate = candidates[0];
    let bestCoordinates = this.getPositionCoordinates(anchorRect, tooltipRect, bestCandidate);
    let bestScore = this.getOverflowScore(bestCoordinates, tooltipRect);

    for (const candidate of candidates.slice(1)) {
      const coordinates = this.getPositionCoordinates(anchorRect, tooltipRect, candidate);
      const score = this.getOverflowScore(coordinates, tooltipRect);
      if (score < bestScore) {
        bestCandidate = candidate;
        bestCoordinates = coordinates;
        bestScore = score;
      }
      if (score === 0) {
        break;
      }
    }

    return {
      coordinates: this.clampToViewport(bestCoordinates, tooltipRect),
      position: bestCandidate,
    };
  }

  show(text: TooltipContent, anchor: Element) {
    const position = this.resolvePreferredPosition(
      (this.options.position || "top") as TooltipPosition,
    );
    this.setContent(text);
    this.setActivePosition(position);
    this.render();
    this.el.style.visibility = "hidden";
    TooltipView.container.appendChild(this.el);
    const anchorRect = anchor.getBoundingClientRect();
    const tooltipRect = this.el.getBoundingClientRect();
    const applyPosition = (nextPosition: { top: string | number; left: string | number }) => {
      Object.entries(nextPosition).forEach(([name, value]) => {
        this.el.style.setProperty(name, typeof value === "number" ? `${value}px` : value);
      });
    };
    const resolvedPosition = this.resolvePosition(anchorRect, tooltipRect, position);
    this.setActivePosition(resolvedPosition.position);
    applyPosition({
      left: resolvedPosition.coordinates.left + window.scrollX,
      top: resolvedPosition.coordinates.top + window.scrollY,
    });
    this.el.style.removeProperty("visibility");
  }

  hide() {
    this.el.remove();
    this.clearEvents();
  }

  destroy() {
    this.hide();
  }
}

export default TooltipView;
