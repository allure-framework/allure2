import { Object as BaseObject } from "backbone.marionette";
import $ from "jquery";

const codes = {
  27: "esc",
  37: "left",
  38: "up",
  39: "right",
  40: "down",
};

class HotkeysService extends BaseObject {
  initialize() {
    $(document).on("keydown", this.keyHandler.bind(this));
  }

  keyHandler(event) {
    const code = codes[event.keyCode];
    if (code) {
      this.trigger(`key:${code}`, event);
    }
  }
}

export default new HotkeysService();
