import { View } from "backbone.marionette";
import { escapeExpression } from "handlebars/runtime";
import { className } from "../../decorators/index";
import translate from "../../helpers/t";

const severities = ["blocker", "critical", "normal", "minor", "trivial"];

@className("pane__section")
class SeverityView extends View {
  template({ severity }) {
    if (!severity) {
      return "";
    }

    if (severities.indexOf(severity) >= 0) {
      return `${translate("testResult.severity.name")}: ${translate(`testResult.severity.${severity}`)}`;
    }

    return `${translate("testResult.severity.name")}: ${escapeExpression(severity)}`;
  }

  serializeData() {
    const extra = this.model.get("extra");
    return {
      severity: extra ? extra.severity : null,
    };
  }
}

export default SeverityView;
