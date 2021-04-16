import "./styles.scss";
import { View } from "backbone.marionette";
import { options } from "../../decorators";
import template from "./LoaderView.hbs";

@options({
  text: "Loading...",
})
class LoaderView extends View {
  template = template;

  initialize(opts) {
    this.options = opts;
  }

  serializeData() {
    return this.options;
  }
}

export default LoaderView;
