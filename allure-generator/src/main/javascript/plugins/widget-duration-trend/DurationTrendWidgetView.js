import "./styles.scss";
import { View } from "backbone.marionette";
import { interpolateRgb } from "d3-interpolate";
import { scaleLinear, scaleOrdinal } from "d3-scale";
import { last } from "underscore";
import TrendChartView from "../../components/graph-trend-chart/TrendChartView";
import { className, regions } from "../../decorators/index";
import duration from "../../helpers/duration";
import template from "./DurationTrendWidgetView.hbs";

@regions({
  chart: ".duration-trend__chart",
})
@className("duration-trend")
class DurationTrendWidgetView extends View {
  template = template;

  onRender() {
    const key = "duration";
    const values = this.model.map((model) => model.get("data")[key]);
    const lastExtremum = Math.min(
      ...[(d) => d >= 0, (d) => d <= 0].map((op) =>
        values.reduce((m, c, i) => (op(c - values[i - 1]) ? i : m), last(values)),
      ),
    );
    const amplitude = Math.max(...values) - Math.min(...values);
    const lastDelta = values[lastExtremum] - last(values);

    const level = scaleLinear().domain([0, Math.max(amplitude, 0.25 * Math.max(...values))]);

    level
      .range(lastDelta > 0 ? ["#c4cac6", "#31a354"] : ["#cdc5c4", "#e34a33"])
      .interpolate(interpolateRgb);

    const colors = scaleOrdinal().range([level(Math.abs(lastDelta))]);

    this.showChildView(
      "chart",
      new TrendChartView({
        model: this.model,
        hidePoints: true,
        hideLines: true,
        colors,
        keys: [key],
        yTickFormat: (d) => duration(d, 2),
      }),
    );
  }
}

export default DurationTrendWidgetView;
