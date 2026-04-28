import "./LanguageSelectView.scss";
import i18next, { LANGUAGES, toI18nextLanguage } from "../../core/i18n/index.mts";
import settings from "../../core/services/settings.mts";
import b from "../../shared/bem/index.mts";
import { createElement } from "../../shared/dom.mts";
import PopoverView from "../../shared/ui/PopoverView.mts";
import gtag from "../../utils/gtag.mts";

type LanguageId = import("../../core/i18n/index.mts").LanguageId;

class LanguageSelectView extends PopoverView {
  constructor() {
    super({
      position: "top-right",
    });
  }

  setContent() {
    const currentLang = settings.getLanguage();
    this.content = createElement("ul", {
      className: b("language-select", "menu"),
      children: LANGUAGES.map(({ id, title }) =>
        createElement("li", {
          attrs: { "data-id": id },
          className: b("language-select", "item", { active: id === currentLang }),
          text: title,
        }),
      ),
    });
  }

  show(anchor: Element): void;
  show(text: string | null | undefined, anchor: Element): void;
  show(textOrAnchor: string | Element | null | undefined, maybeAnchor?: Element) {
    const anchor = textOrAnchor instanceof Element ? textOrAnchor : maybeAnchor;
    if (!anchor) {
      return;
    }

    super.show(null, anchor);
    setTimeout(() => {
      document.addEventListener("click", () => this.hide(), { once: true });
    });
  }

  onLanguageClick(event: Event) {
    const langId = (event.currentTarget as HTMLElement | null)?.dataset.id as
      | LanguageId
      | undefined;
    const language = LANGUAGES.find(({ id }) => id === langId);
    if (!language) {
      return;
    }

    settings.setLanguage(language.id);
    i18next.changeLanguage(toI18nextLanguage(language.id));
    gtag("language_change", {
      language: language.id,
    });
  }

  className = "language-select popover";

  getDelegatedEvents() {
    return {
      "click .language-select__item": "onLanguageClick",
    };
  }
}

export default LanguageSelectView;
