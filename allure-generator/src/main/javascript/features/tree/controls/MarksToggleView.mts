import "./MarksToggleView.scss";
import { bindDelegatedEvents } from "../../../core/view/domEvents.mts";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import { createAllureIconElement } from "../../../helpers/allure-icon.mts";
import translate from "../../../helpers/t.mts";
import b from "../../../shared/bem/index.mts";
import { createElement } from "../../../shared/dom.mts";
import TooltipView from "../../../shared/ui/TooltipView.mts";
import gtag from "../../../utils/gtag.mts";
import { values } from "../../../utils/marks.mts";

type MarksToggleOptions = {
  settings: ReturnType<
    typeof import("../../../core/services/settings.mts").getSettingsForTreePlugin
  >;
};

const createMarksToggleView = (options: MarksToggleOptions) => {
  const tooltip = new TooltipView({ position: "bottom" });
  const el = defineMountableElement(document.createElement("div"), {
    settings: options.settings,
    tooltip,
    onToggleMark(event: Event) {
      const element = event.currentTarget as HTMLElement;
      const name = element.dataset.mark;
      if (!name) {
        return;
      }
      const checked = element.classList.contains("n-label-mark");
      const marks = options.settings.getVisibleMarks() || {
        flaky: false,
        newFailed: false,
        newPassed: false,
        newBroken: false,
        retriesStatusChange: false,
      };
      options.settings.setVisibleMarks(
        Object.assign({}, marks, {
          [name]: checked,
        }),
      );
      gtag("marks_toggle_click", {
        status: name,
        checked,
      });
    },
    onTooltipHover(event: Event) {
      const element = event.currentTarget as HTMLElement;
      tooltip.show(element.dataset.tooltip || "", element);
    },
    onTooltipLeave() {
      tooltip.hide();
    },
  });
  let releaseEvents = () => {};
  let releaseSettings = () => {};

  Object.assign(el, {
    render() {
      releaseEvents();
      const marks = options.settings.getVisibleMarks() || {
        flaky: false,
        newFailed: false,
        newPassed: false,
        newBroken: false,
        retriesStatusChange: false,
      };
      el.className = "marks-toggle";
      el.replaceChildren(
        createElement("div", {
          className: b("marks-toggle", "items"),
          children: [
            createElement("span", {
              className: b("marks-toggle", "label"),
              text: `${translate("component.tree.filter-marks")}:`,
            }),
            values.map((mark) => {
              const markName = translate(`marks.${mark}`, {});
              const active = !!marks[mark];
              const action = active ? "hideCases" : "showCases";
              const labelClass = active ? "y-label-mark" : "n-label-mark";
              const modifierClass = active ? "y-label" : "n-label";
              const tooltipText = translate(`component.markToggle.${action}`, {
                hash: { mark: markName },
              });
              const icon = createAllureIconElement(mark, { noTooltip: true, size: "s" });

              return createElement("div", {
                className: b("marks-toggle", "item"),
                children: createElement("span", {
                  attrs: {
                    "data-mark": mark,
                    "data-tooltip": tooltipText,
                  },
                  className: `${labelClass} ${modifierClass}_mark_${mark}`,
                  children: icon || "",
                }),
              });
            }),
          ],
        }),
      );
      releaseEvents = bindDelegatedEvents({
        root: el,
        events: {
          "click .y-label-mark, .n-label-mark": "onToggleMark",
          "mouseenter [data-tooltip]": "onTooltipHover",
          "mouseleave [data-tooltip]": "onTooltipLeave",
        },
        context: el,
      });
      return el;
    },
    destroy() {
      releaseEvents();
      tooltip.hide();
      releaseSettings();
      el.remove();
    },
  });
  const rerender = () => el.render?.();

  releaseSettings = options.settings.subscribe(rerender);

  return el;
};

export default createMarksToggleView;
