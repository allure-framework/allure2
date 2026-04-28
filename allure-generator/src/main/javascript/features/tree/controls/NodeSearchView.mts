import "./NodeSearchView.scss";
import { bindDelegatedEvents } from "../../../core/view/domEvents.mts";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import { createElement } from "../../../shared/dom.mts";
import gtag from "../../../utils/gtag.mts";
import { SEARCH_QUERY_KEY } from "../model/searchState.mts";

type NodeSearchOptions = {
  state: import("../../../core/state/StateStore.mts").default;
};

const createNodeSearchView = (options: NodeSearchOptions) => {
  const el = defineMountableElement(document.createElement("div"), {
    state: options.state,
    onChangeSorting(event: Event) {
      const target = event.target as HTMLInputElement;
      options.state.set(SEARCH_QUERY_KEY, target.value);
      gtag("search");
    },
    close() {
      options.state.set(SEARCH_QUERY_KEY, "");
    },
  });
  let releaseEvents = () => {};

  Object.assign(el, {
    render() {
      releaseEvents();
      el.className = "search";
      el.replaceChildren(
        createElement("div", {
          className: "search__container",
          children: createElement("input", {
            attrs: { type: "text" },
            className: "search__input",
          }),
        }),
      );
      releaseEvents = bindDelegatedEvents({
        root: el,
        events: {
          "input input": "onChangeSorting",
        },
        context: el,
      });
      const input = el.querySelector("input");
      if (input instanceof HTMLInputElement) {
        const value = options.state.get(SEARCH_QUERY_KEY);
        input.value = typeof value === "string" ? value : "";
      }
      return el;
    },
    destroy() {
      releaseEvents();
      el.remove();
    },
  });

  return el;
};

export default createNodeSearchView;
