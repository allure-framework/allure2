import { View } from "backbone.marionette";
import { className } from "../../decorators/index";
import template from "./OwnerView.hbs";

@className("pane__section")
class OwnerView extends View {
  template = template;

  serializeData() {
    const extra = this.model.get("extra");
    return {
      owner: extra ? extra.owner : null,
    };
  }
}

export default OwnerView;
