import { createElement, createFragment, createSvgElement } from "../../../shared/dom.mts";

type TimelineTemplateOptions = {
  BRUSH_VIEW_HEIGHT: number;
  PADDING: number;
};

export const createTimelineViewContent = ({
  BRUSH_VIEW_HEIGHT,
  PADDING,
}: TimelineTemplateOptions) =>
  createFragment(
    createElement("div", {
      className: "timeline__body",
      children: createElement("div", {
        className: "timeline__chart",
        children: createSvgElement("svg", {
          className: "timeline__chart_svg",
          children: createSvgElement("g", {
            attrs: { transform: `translate(${PADDING}, 15)` },
            children: [
              createSvgElement("g", {
                className: "timeline__slider",
              }),
              createSvgElement("g", {
                attrs: { transform: `translate(0, ${PADDING})` },
                className: "timeline__plot",
                children: createSvgElement("g", {
                  className: "timeline__chart__axis timeline__chart__axis_x",
                }),
              }),
            ],
          }),
        }),
      }),
    }),
    createElement("div", {
      className: "timeline__brush",
      children: createSvgElement("svg", {
        className: "timeline__brush_svg",
        attrs: { height: String(BRUSH_VIEW_HEIGHT) },
        children: createSvgElement("g", {
          className: "timeline__brush__axis timeline__brush__axis_x",
        }),
      }),
    }),
  );
