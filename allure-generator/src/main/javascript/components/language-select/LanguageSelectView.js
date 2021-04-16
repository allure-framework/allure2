import "./styles.scss";
import PopoverView from "app/components/popover/PopoverView";
import { className, on } from "app/decorators";
import settings from "app/utils/settings";
import i18next, { LANGUAGES } from "app/utils/translation";
import $ from "jquery";
import template from "./LanguageSelectView.hbs";

@className("language-select popover")
class LanguageSelectView extends PopoverView {
  initialize() {
    super.initialize({ position: "right" });
  }

  setContent() {
    this.$el.html(
      template({
        languages: LANGUAGES,
        currentLang: settings.getLanguage(),
      }),
    );
  }

  show(anchor) {
    super.show(null, anchor);
    this.delegateEvents();
    setTimeout(() => {
      $(document).one("click", () => this.hide());
    });
  }

  @on("click .language-select__item")
  onLanguageClick(e) {
    const langId = this.$(e.currentTarget).data("id");
    settings.setLanguage(langId);
    i18next.changeLanguage(langId);
  }
}

export default LanguageSelectView;
