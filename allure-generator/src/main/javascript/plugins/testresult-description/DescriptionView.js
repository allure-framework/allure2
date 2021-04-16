import { View } from "backbone.marionette";
import { className } from "../../decorators";
import template from "./DescriptionView.hbs";

@className("pane__section")
class DescriptionView extends View {
  template = template;

  serializeData() {
    return {
      descriptionHtml: this.model.get("descriptionHtml"),
    };
  }
}

export default DescriptionView;
