import { View } from "backbone.marionette";
import template from "./WidgetStatusView.hbs";

export default class WidgetStatusView extends View {
  template = template;

  serializeData() {
    const showLinks = typeof this.showLinks !== "undefined" ? this.showLinks : true;
    const showAll = typeof this.showAll !== "undefined" ? this.showAll : true;
    return Object.assign(super.serializeData(), {
      rowTag: showLinks ? "a" : "span",
      title: this.title,
      showAll: showAll,
      baseUrl: this.baseUrl,
    });
  }
}
