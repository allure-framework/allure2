import "./styles.scss";
import { View } from "backbone.marionette";
import $ from "jquery";
import { className, on, regions } from "../../decorators/index";
import template from "./ModalView.hbs";

@className("modal")
@regions({
  content: ".modal__content",
})
class ModalView extends View {
  template = template;
  static container = $(document.body);

  show() {
    this.constructor.container.append(this.$el);
    this.showChildView("content", this.options.childView);
    $("#content").toggleClass("blur", true);
  }

  onDestroy() {
    $("#content").toggleClass("blur", false);
  }

  @on("click .modal__content")
  onKeepOpen(e) {
    e.stopPropagation();
  }

  @on("click .modal__background, .modal__close")
  onClose() {
    this.destroy();
  }

  serializeData() {
    return {
      cls: this.className,
      title: this.options.title,
    };
  }
}

export default ModalView;
