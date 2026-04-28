import "./styles/ErrorSplashView.scss";
import { defineMountableElement } from "../../core/view/elementView.mts";
import b from "../bem/index.mts";
import { createElement } from "../dom.mts";

type ErrorSplashOptions = {
  code: string | number;
  message: string;
};

const ErrorSplashView = (options: ErrorSplashOptions) => {
  const el = defineMountableElement(document.createElement("div"), {});

  Object.assign(el, {
    render() {
      const cls = "error-splash";
      el.className = cls;
      el.replaceChildren(
        createElement("div", {
          className: b(cls),
          children: [
            createElement("h1", {
              className: b(cls, "title"),
              text: options?.code ?? "",
            }),
            createElement("p", {
              className: b(cls, "message"),
              text: options?.message || "",
            }),
          ],
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

export default ErrorSplashView;
