import "./styles.scss";
import { View } from "backbone.marionette";
import { className } from "../../decorators";
import template from "./LinksView.hbs";

@className("pane__section")
class LinksView extends View {
  template = template;

  serializeData() {
    return {
      links: this.model.get("links"),
    };
  }
}

export default LinksView;
