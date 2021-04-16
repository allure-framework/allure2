import "./styles.scss";
import { View } from "backbone.marionette";
import split from "split.js";
import { className, regions } from "../../decorators";
import settings from "../../utils/settings";
import template from "./SideBySideView.hbs";

@className("side-by-side")
@regions({
  left: ".side-by-side__left",
  right: ".side-by-side__right",
})
class SideBySideView extends View {
  template = template;

  onAttach() {
    const splitter = split([".side-by-side__left", ".side-by-side__right"], {
      gutterSize: 7,
      sizes: settings.getSideBySidePosition(),
      onDragEnd: function() {
        settings.setSideBySidePosition(splitter.getSizes());
      },
    });
  }

  onRender() {
    const { left, right } = this.options;
    this.showChildView("left", left);
    this.showChildView("right", right);
  }

  templateContext() {
    return {
      cls: "side-by-side",
    };
  }
}

export default SideBySideView;
