import { View } from "backbone.marionette";
import { className } from "../../decorators";
import template from "./SeverityView.hbs";

@className("pane__section")
class SeverityView extends View {
  template = template;

  serializeData() {
    const extra = this.model.get("extra");
    return {
      severity: extra ? extra.severity : null,
    };
  }
}

export default SeverityView;
