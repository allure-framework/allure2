import { createElement, createFragment, createSvgElement } from "../../../shared/dom.mts";

const statuses = ["failed", "broken", "passed", "skipped", "unknown"] as const;

type TimelineTemplateOptions = {
  BRUSH_VIEW_HEIGHT: number;
  PADDING: number;
};

const createRetryPattern = (status: (typeof statuses)[number]) =>
  createSvgElement("pattern", {
    attrs: {
      height: "8",
      id: `timeline-retry-${status}`,
      patternTransform: "rotate(45)",
      patternUnits: "userSpaceOnUse",
      width: "8",
    },
    className: `timeline__retry-pattern timeline__retry-pattern_status_${status}`,
    children: [
      createSvgElement("rect", {
        attrs: { height: "8", width: "8", x: "0", y: "0" },
        className: "timeline__retry-pattern-base",
      }),
      createSvgElement("rect", {
        attrs: { height: "8", width: "3", x: "0", y: "0" },
        className: "timeline__retry-pattern-stripe",
      }),
    ],
  });

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
          children: [
            createSvgElement("defs", {
              children: statuses.map(createRetryPattern),
            }),
            createSvgElement("g", {
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
          ],
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
