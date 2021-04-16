import "./styles.scss";
import { View } from "backbone.marionette";
import PieChartView from "../../components/graph-pie-chart/PieChartView";
import { regions } from "../../decorators";
import template from "./SummaryWidgetView.hbs";

@regions({
  chart: ".summary-widget__chart",
})
class SummaryWidgetView extends View {
  template = template;

  onRender() {
    this.showChildView(
      "chart",
      new PieChartView({
        model: this.model,
        showLegend: false,
      }),
    );
  }

  serializeData() {
    const testRuns = this.model.get("testRuns");
    const length = testRuns && testRuns.length;
    return Object.assign(super.serializeData(), {
      isAggregated: length > 1,
      launchesCount: length,
    });
  }
}

export default SummaryWidgetView;
