import "./SideNavView.scss";
import BaseElement from "../../core/elements/BaseElement.mts";
import { LANGUAGES } from "../../core/i18n/index.mts";
import { getTabs } from "../../core/registry/index.mts";
import router from "../../core/routing/router.mts";
import settings from "../../core/services/settings.mts";
import translate from "../../helpers/t.mts";
import b from "../../shared/bem/index.mts";
import { createElement } from "../../shared/dom.mts";
import { createIconElement } from "../../shared/icon/index.mts";
import TooltipView from "../../shared/ui/TooltipView.mts";
import { findWhere } from "../../shared/utils/collections.mts";
import LanguageSelectView from "./LanguageSelectView.mts";

class SideNavCustomElement extends BaseElement {
  declare tooltip: TooltipView;

  declare langSelect: LanguageSelectView;

  constructor() {
    super();
    this.tooltip = new TooltipView({ position: "right" });
    this.langSelect = new LanguageSelectView();
  }

  renderElement() {
    const language = findWhere(LANGUAGES, { id: settings.getLanguage() }) || LANGUAGES[0];
    const isCollapsed = settings.isSidebarCollapsed();
    if (!language) {
      throw new Error("At least one language must be configured");
    }
    const languageButtonClass = `${b("button")} ${b("button", { inverse: true })} ${b(
      "side-nav",
      "language-small",
      { lang: language.id },
    )}`;

    this.className = "side-nav";
    this.classList.toggle("side-nav_collapsed", isCollapsed);
    this.replaceChildren(
      createElement("div", {
        className: b("side-nav", "head"),
        children: createElement("a", {
          attrs: {
            "data-ga4-event": "home_click",
            href: "#",
          },
          className: b("side-nav", "brand"),
          children: [
            createElement("span", {
              className: b("side-nav", "icon-lane"),
              children: createElement("span", {
                className: b("side-nav", "brand-icon"),
              }),
            }),
            createElement("span", {
              className: `${b("side-nav", "text")} ${b("side-nav", "brand-text")}`,
              text: "Allure",
            }),
          ],
        }),
      }),
      createElement("ul", {
        className: b("side-nav", "menu"),
        children: getTabs().map(({ tabName, icon, title }) => {
          const linkClass = b("side-nav", "link", { active: this.isTabActive(tabName) });
          return createElement("li", {
            attrs: {
              "data-ga4-event": "tab_click",
              "data-ga4-param-tab": tabName,
              "data-tooltip": translate(title),
            },
            className: b("side-nav", "item"),
            children: createElement("a", {
              attrs: { href: `#${tabName}` },
              className: linkClass,
              children: [
                createElement("span", {
                  className: b("side-nav", "icon-lane"),
                  children: createIconElement(icon, {
                    className: b("side-nav", "icon"),
                    size: "m",
                  }),
                }),
                createElement("span", {
                  className: b("side-nav", "text"),
                  text: translate(title),
                }),
              ],
            }),
          });
        }),
      }),
      createElement("div", {
        className: b("side-nav", "strut"),
      }),
      createElement("div", {
        className: b("side-nav", "footer"),
        children: [
          createElement("div", {
            attrs: {
              "data-tooltip": translate("controls.language"),
            },
            className: b("side-nav", "item"),
            children: createElement("button", {
              attrs: {
                "data-ga4-event": "language_menu_click",
              },
              className: languageButtonClass,
              text: language.id,
            }),
          }),
          createElement("div", {
            attrs: {
              "data-ga4-event": "expand_menu_click",
              "data-tooltip": translate("controls.expand"),
            },
            className: b("side-nav", "item"),
            children: createElement("div", {
              className: b("side-nav", "collapse"),
              children: [
                createElement("span", {
                  className: b("side-nav", "icon-lane"),
                  children: createIconElement("lineArrowsChevronRight", {
                    className: `${b("side-nav", "icon")} ${b("side-nav", "collapse-icon")}`,
                    size: "m",
                  }),
                }),
                createElement("span", {
                  attrs: isCollapsed ? { hidden: "" } : {},
                  className: b("side-nav", "text"),
                  text: translate("controls.collapse"),
                }),
              ],
            }),
          }),
        ],
      }),
    );
    this.bindEvents(
      {
        "click .side-nav__brand": "onNavigationClick",
        "click .side-nav__link": "onNavigationClick",
        "mouseenter [data-tooltip]": "onSidelinkHover",
        "mouseleave [data-tooltip]": "onSidelinkLeave",
        "click .side-nav__collapse": "onCollapseClick",
        "click .side-nav__language": "onLanguageClick",
        "click .side-nav__language-small": "onLanguageClick",
      },
      this,
    );

    return this;
  }

  isTabActive(name: string) {
    const currentUrl = router.getCurrentUrl();
    return name ? currentUrl.indexOf(name) === 0 : currentUrl === name;
  }

  onNavigationClick(event: Event) {
    if (
      event instanceof MouseEvent &&
      (event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey)
    ) {
      return;
    }

    const element = event.currentTarget as HTMLAnchorElement | null;
    const href = element?.getAttribute("href");
    if (!href) {
      return;
    }

    event.preventDefault();
    router.toUrl(href);
    this.tooltip.hide();
  }

  onSidelinkHover(event: Event) {
    if (this.classList.contains("side-nav_collapsed")) {
      const element = event.currentTarget as HTMLElement;
      this.tooltip.show(element.dataset.tooltip || "", element);
    }
  }

  onSidelinkLeave() {
    this.tooltip.hide();
  }

  onCollapseClick() {
    this.classList.toggle("side-nav_collapsed");
    const collapsed = this.classList.contains("side-nav_collapsed");
    settings.setSidebarCollapsed(collapsed);
    const collapseText = this.querySelector(".side-nav__collapse .side-nav__text");
    if (collapseText instanceof HTMLElement) {
      collapseText.toggleAttribute("hidden", collapsed);
    }
    this.tooltip.hide();
  }

  onLanguageClick(event: Event) {
    const element = event.currentTarget as HTMLElement;
    if (this.langSelect.isVisible()) {
      this.langSelect.hide();
    } else {
      this.langSelect.show(element);
    }
    this.tooltip.hide();
  }

  destroy() {
    this.tooltip.hide();
    this.langSelect.hide();
    super.destroy();
  }
}

if (!customElements.get("allure-side-nav")) {
  customElements.define("allure-side-nav", SideNavCustomElement);
}

const createSideNavElement = (options: Record<string, never> = {}) => {
  const element = document.createElement("allure-side-nav") as SideNavCustomElement;
  element.setOptions(options);
  return element;
};

export default createSideNavElement;
