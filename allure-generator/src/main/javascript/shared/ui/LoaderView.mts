import "./styles/LoaderView.scss";
import { defineMountableElement } from "../../core/view/elementView.mts";
import translate from "../../helpers/t.mts";
import { createElement } from "../dom.mts";

type LoaderVariant = "inline" | "screen";

type LoaderOptions = {
  spinner?: Node;
  showText?: boolean;
  text?: string;
  variant?: LoaderVariant;
};

const SVG_NS = "http://www.w3.org/2000/svg";

const createSvgNode = (tagName: string, attrs: Record<string, string>) => {
  const node = document.createElementNS(SVG_NS, tagName);
  Object.entries(attrs).forEach(([name, value]) => node.setAttribute(name, value));
  return node;
};

const createDefaultSpinner = () => {
  const svg = createSvgNode("svg", {
    "aria-hidden": "true",
    class: "loader__spinner",
    fill: "none",
    viewBox: "0 0 16 16",
  });
  svg.append(
    createSvgNode("circle", {
      class: "loader__spinner-track",
      cx: "8",
      cy: "8",
      r: "7",
      stroke: "currentColor",
      "stroke-width": "1.5",
      "vector-effect": "non-scaling-stroke",
    }),
    createSvgNode("path", {
      class: "loader__spinner-segment",
      d: "M 15 8 A 7 7 0 0 1 8 15",
      stroke: "currentColor",
      "stroke-width": "1.5",
      "vector-effect": "non-scaling-stroke",
    }),
  );
  return svg;
};

const LoaderView = (options: LoaderOptions = {}) => {
  const el = defineMountableElement(document.createElement("div"), {});

  Object.assign(el, {
    render() {
      const { spinner, text, variant = "inline", showText = Boolean(text) } = options;
      const resolvedText = text || translate("controls.loading");
      const textClass = showText ? "loader__text" : "loader__text loader__text_hidden";

      el.className = `loader-view loader-view_${variant}`;
      el.replaceChildren(
        createElement("div", {
          className: `loader__mask loader__mask_${variant}`,
          children: createElement("div", {
            attrs: {
              "aria-label": resolvedText,
              "aria-live": "polite",
              role: "status",
            },
            className: `loader loader_${variant}`,
            children: [
              spinner || createDefaultSpinner(),
              createElement("p", {
                className: textClass,
                text: resolvedText,
              }),
            ],
          }),
        }),
      );
      return el;
    },
    destroy() {
      el.remove();
    },
  });

  return el;
};

export default LoaderView;
