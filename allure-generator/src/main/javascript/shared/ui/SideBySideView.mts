import "./styles/SideBySideView.scss";
import split from "split.js";
import BaseElement from "../../core/elements/BaseElement.mts";
import settings from "../../core/services/settings.mts";
import { createElement } from "../dom.mts";
import type { Mountable } from "../../core/view/types.mts";
import gtag from "../../utils/gtag.mts";

type SideBySideOptions = {
  left: Mountable;
  right: Mountable;
};

class SideBySideElement extends BaseElement {
  declare options: SideBySideOptions;

  declare splitter: {
    destroy?: () => void;
    getSizes: () => number[];
  } | null;

  constructor() {
    super();
    this.splitter = null;
  }

  renderElement() {
    this.destroySplitter();
    this.className = "side-by-side";
    this.replaceChildren(
      createElement("div", { className: "side-by-side__left" }),
      createElement("div", { className: "side-by-side__right" }),
    );
    this.mountChild("left", this.options.left, ".side-by-side__left");
    this.mountChild("right", this.options.right, ".side-by-side__right");

    if (this.isConnected) {
      this.setupSplitter();
    }

    return this;
  }

  attachToDom() {
    super.attachToDom();
    this.setupSplitter();
  }

  setupSplitter() {
    if (this.splitter) {
      return;
    }

    const leftPane = this.querySelector<HTMLElement>(".side-by-side__left");
    const rightPane = this.querySelector<HTMLElement>(".side-by-side__right");

    if (!leftPane || !rightPane) {
      throw new Error("Split panes must be rendered before the splitter is initialized");
    }

    this.splitter = split([leftPane, rightPane], {
      gutterSize: 7,
      sizes: settings.getSideBySidePosition(),
      onDragEnd: () => {
        if (!this.splitter) {
          return;
        }

        const [left = 50, right = 50] = this.splitter.getSizes();
        const sizes: [number, number] = [left, right];
        settings.setSideBySidePosition(sizes);
        gtag("side-by-side-resize", {
          sizes,
        });
      },
    });
  }

  destroySplitter() {
    this.splitter?.destroy?.();
    this.splitter = null;
  }

  destroy() {
    this.destroySplitter();
    super.destroy();
  }
}

if (!customElements.get("allure-side-by-side")) {
  customElements.define("allure-side-by-side", SideBySideElement);
}

const createSideBySideView = (options?: SideBySideOptions) => {
  const element = document.createElement("allure-side-by-side") as SideBySideElement;
  element.setOptions(options);
  return element;
};

export { SideBySideElement };

export default createSideBySideView;
