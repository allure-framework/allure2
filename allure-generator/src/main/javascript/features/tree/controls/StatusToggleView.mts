import "./StatusToggleView.scss";
import { bindDelegatedEvents } from "../../../core/view/domEvents.mts";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import translate from "../../../helpers/t.mts";
import b from "../../../shared/bem/index.mts";
import { createElement } from "../../../shared/dom.mts";
import TooltipView from "../../../shared/ui/TooltipView.mts";
import gtag from "../../../utils/gtag.mts";
import { values } from "../../../utils/statuses.mts";

type StatusToggleOptions = {
  settings: ReturnType<
    typeof import("../../../core/services/settings.mts").getSettingsForTreePlugin
  >;
  statistic?: Record<string, number>;
};

const createStatusToggleView = (options: StatusToggleOptions) => {
  const tooltip = new TooltipView({ position: "bottom" });
  const el = defineMountableElement(document.createElement("div"), {
    settings: options.settings,
    statistic: options.statistic,
    tooltip,
    onToggleStatus(event: Event) {
      const element = event.currentTarget as HTMLElement;
      const name = element.dataset.status;
      if (!name) {
        return;
      }
      const checked = element.classList.contains("n-label");
      const statuses = options.settings.getVisibleStatuses() || {
        failed: true,
        broken: true,
        skipped: true,
        unknown: true,
        passed: true,
      };
      options.settings.setVisibleStatuses(
        Object.assign({}, statuses, {
          [name]: checked,
        }),
      );
      gtag("status_toggle_click", {
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
      const statuses = options.settings.getVisibleStatuses() || {
        failed: true,
        broken: true,
        skipped: true,
        unknown: true,
        passed: true,
      };
      el.className = "status-toggle";
      el.replaceChildren(
        createElement("div", {
          className: b("status-toggle", "items"),
          children: [
            createElement("span", {
              className: b("status-toggle", "label"),
              text: `${translate("component.tree.filter")}:`,
            }),
            values.map((status) => {
              const statusName = translate(`status.${status}`, {});
              const active = !!statuses[status];
              const count = options.statistic ? options.statistic[status.toLowerCase()] : 0;
              const action = active ? "hideCases" : "showCases";
              const labelClass = active ? "y-label" : "n-label";
              const tooltipText = translate(`component.statusToggle.${action}`, {
                hash: { status: statusName },
              });

              return createElement("div", {
                className: b("status-toggle", "item"),
                children: createElement("span", {
                  attrs: {
                    "data-status": status,
                    "data-tooltip": tooltipText,
                  },
                  className: `${labelClass} ${labelClass}_status_${status}`,
                  text: count,
                }),
              });
            }),
          ],
        }),
      );
      releaseEvents = bindDelegatedEvents({
        root: el,
        events: {
          "click .y-label, .n-label": "onToggleStatus",
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

export default createStatusToggleView;
