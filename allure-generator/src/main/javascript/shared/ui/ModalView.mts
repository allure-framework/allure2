import "./styles/ModalView.scss";
import BaseElement from "../../core/elements/BaseElement.mts";
import type { Mountable } from "../../core/view/types.mts";
import b from "../bem/index.mts";
import { createElement } from "../dom.mts";
import { createIconElement } from "../icon/index.mts";
import translate from "../../helpers/t.mts";
import TooltipView from "./TooltipView.mts";

type ModalOptions = {
  childView: Mountable;
  title: string;
};

class ModalElement extends BaseElement {
  static container = document.body;

  declare options: ModalOptions;

  declare tooltip: TooltipView;

  constructor() {
    super();
    this.tooltip = new TooltipView({
      position: "bottom",
    });
  }

  show() {
    this.render();
    (this.constructor as typeof ModalElement).container.appendChild(this);
    this.attachToDom();
    document.querySelector("#content")?.classList.add("blur");
    return this;
  }

  renderElement() {
    const { title } = this.options;

    this.className = "modal";
    this.replaceChildren(
      createElement("div", {
        className: "modal__background",
        children: createElement("div", {
          className: "modal__window",
          children: [
            createElement("h2", {
              className: b("modal", "title"),
              children: [
                createElement("span", { text: title }),
                createIconElement("lineGeneralXClose", {
                  attributes: {
                    "data-tooltip": translate("controls.close"),
                  },
                  className: b("modal", "close"),
                  size: "m",
                }),
              ],
            }),
            createElement("div", {
              className: "modal__content",
            }),
            createElement("br"),
          ],
        }),
      }),
    );
    this.bindEvents(
      {
        "click .modal__content": "onKeepOpen",
        "click .modal__background, .modal__close": "onClose",
        "mouseenter [data-tooltip]": "onTooltipHover",
        "mouseleave [data-tooltip]": "onTooltipLeave",
      },
      this,
    );
    this.mountChild("content", this.options.childView, ".modal__content");
    return this;
  }

  onKeepOpen(e: Event) {
    e.stopPropagation();
  }

  onClose() {
    this.destroy();
  }

  onTooltipHover(e: Event) {
    const element = e.currentTarget as HTMLElement;
    this.tooltip.show(element.dataset.tooltip || "", element);
  }

  onTooltipLeave() {
    this.tooltip.hide();
  }

  destroy() {
    document.querySelector("#content")?.classList.remove("blur");
    this.tooltip.hide();
    super.destroy();
  }
}

if (!customElements.get("allure-modal")) {
  customElements.define("allure-modal", ModalElement);
}

const createModalView = (options?: ModalOptions) => {
  const element = document.createElement("allure-modal") as ModalElement;
  element.setOptions(options);
  return element;
};

export default createModalView;
