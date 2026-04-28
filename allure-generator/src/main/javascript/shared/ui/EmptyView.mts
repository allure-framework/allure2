import "./styles/EmptyView.scss";
import { defineMountableElement } from "../../core/view/elementView.mts";
import b from "../bem/index.mts";
import { createElement } from "../dom.mts";

type EmptyOptions = {
  message: string;
};

const EmptyView = (options: EmptyOptions) => {
  const el = defineMountableElement(document.createElement("div"), {});

  Object.assign(el, {
    render() {
      const cls = "empty-view";
      el.className = cls;
      el.replaceChildren(
        createElement("div", {
          className: b(cls),
          children: createElement("p", {
            className: b(cls, "message"),
            text: options?.message || "",
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

export default EmptyView;
