import { View } from "backbone.marionette";
import { className } from "../../decorators/index";
import translate from "../../helpers/t";

@className("pane__section")
class SeverityView extends View {
  template({ severity }) {
    return severity
      ? `${translate("testResult.severity.name")}: ${translate(`testResult.severity.${severity}`)}`
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
