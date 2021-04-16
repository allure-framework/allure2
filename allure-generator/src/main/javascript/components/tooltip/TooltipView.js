import "./styles.scss";
import { className } from "app/decorators";
import bem from "b_";
import { View } from "backbone";
import $ from "jquery";
import { defaults } from "underscore";

export const POSITION = {
  "top": function({ top, left, width }, { offset }, tipSize) {
    return {
      top: top - tipSize.height - offset,
      left: left + width / 2 - tipSize.width / 2,
    };
  },

  "center": function({ top, left, height, width }, offsets, tipSize) {
    return {
      top: top + height / 2,
      left: left + width / 2 - tipSize.width / 2,
    };
  },
  "right": function({ top, left, height, width }, { offset }, tipSize) {
    return {
      top: top + height / 2 - tipSize.height / 2,
      left: left + width + offset,
    };
  },
  "left": function({ top, left, height }, { offset }, tipSize) {
    return {
      top: top + height / 2 - tipSize.height / 2,
      left: left - offset - tipSize.width,
    };
  },
  "bottom": function({ top, left, height, width }, { offset }, tipSize) {
    return {
      top: top + height + offset,
      left: left + width / 2 - tipSize.width / 2,
    };
  },
  "bottom-left": function({ top, left, height, width }, { offset }, tipSize) {
    return {
      top: top + height + offset,
      left: left + width - tipSize.width,
    };
  },
};

@className("tooltip")
class TooltipView extends View {
  static container = $(document.body);

  initialize(options) {
    this.options = options;
    defaults(this.options, { offset: 10 });
  }

  render() {
    this.constructor.container.append(this.$el);
  }

  isVisible() {
    return this.$el.is(":visible");
  }

  setContent(text) {
    this.$el.html(text);
  }

  show(text, anchor) {
    const { position } = this.options;
    this.setContent(text);
    this.$el.addClass(bem(this.className, { position }));
    this.render();
    if (document.dir === "rtl" && position === "right") {
      this.$el.css(
        POSITION["left"](
          anchor[0].getBoundingClientRect(),
          { offset: this.options.offset },
          this.$el[0].getBoundingClientRect(),
        ),
      );
    } else if (document.dir === "rtl" && position === "left") {
      this.$el.css(
        POSITION["right"](
          anchor[0].getBoundingClientRect(),
          { offset: this.options.offset },
          this.$el[0].getBoundingClientRect(),
        ),
      );
    } else {
      this.$el.css(
        POSITION[position](
          anchor[0].getBoundingClientRect(),
          { offset: this.options.offset },
          this.$el[0].getBoundingClientRect(),
        ),
      );
    }
  }

  hide() {
    this.$el.remove();
  }
}

export default TooltipView;
