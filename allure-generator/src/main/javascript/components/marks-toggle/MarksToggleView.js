import "./styles.scss";
import { View } from "backbone.marionette";
import { className, on } from "../../decorators";
import translate from "../../helpers/t";
import { values } from "../../utils/marks";
import template from "./MarksToggleView.hbs";

@className("marks-toggle")
class MarksToggleView extends View {
  template = template;

  initialize({ settings }) {
    this.settings = settings;
    this.listenTo(settings, "change", this.render);
  }

  serializeData() {
    const marks = this.settings.getVisibleMarks();
    return {
      marks: values.map((mark) => ({
        mark,
        markName: translate(`marks.${mark}`, {}),
        active: !!marks[mark],
        count: this.statistic ? this.statistic[status.toLowerCase()] : 0,
      })),
    };
  }

  @on("click .y-label-mark, .n-label-mark")
  onToggleMark(e) {
    const el = this.$(e.currentTarget);
    const name = el.data("mark");
    const checked = el.hasClass("n-label-mark");
    const marks = this.settings.getVisibleMarks();
    this.settings.setVisibleMarks(Object.assign({}, marks, { [name]: checked }));
  }
}

export default MarksToggleView;
