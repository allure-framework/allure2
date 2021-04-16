import "./styles.scss";
import { View } from "backbone.marionette";
import { className } from "../../decorators";
import template from "./EmptyView.hbs";

@className("empty-view")
class EmptyView extends View {
  template = template;

  serializeData() {
    return {
      cls: this.className,
      message: this.options.message,
    };
  }
}

export default EmptyView;
