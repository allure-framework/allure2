import { View } from "backbone.marionette";
import { escapeExpression } from "handlebars/runtime";
import { className } from "../../decorators/index";
import translate from "../../helpers/t";

@className("pane__section")
class SeverityView extends View {
  template(data) {
    return data.severity
      ? `${translate("testResult.severity.name")}: ${escapeExpression(data.severity)}`
      : "";
  }

  serializeData() {
    const extra = this.model.get("extra");
    return {
      severity: extra ? extra.severity : null,
    };
  }
}

export default SeverityView;
