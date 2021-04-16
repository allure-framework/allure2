import "./styles.scss";
import { View } from "backbone.marionette";
import { scaleOrdinal } from "d3-scale";
import TrendChartView from "../../components/graph-trend-chart/TrendChartView";
import { className, regions } from "../../decorators/index";
import { values } from "../../utils/statuses";
import template from "./HistoryTrendWidgetView.hbs";

@regions({
  chart: ".history-trend__chart",
})
@className("history-trend")
class HistoryTrendWidgetView extends View {
  template = template;

  onRender() {
    this.showChildView(
      "chart",
      new TrendChartView({
        model: this.model,
        hideLines: true,
        hidePoints: true,
        colors: scaleOrdinal(["#fd5a3e", "#ffd050", "#97cc64", "#aaa", "#d35ebe"]).domain(values),
        keys: values,
      }),
    );
  }
}

export default HistoryTrendWidgetView;
