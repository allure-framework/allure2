import { View } from "backbone.marionette";
import { on } from "../../decorators";
import template from "./EnvironmentWidget.hbs";

export default class EnvironmentWidget extends View {
  template = template;

  initialize() {
    this.listLimit = 5;
  }

  @on("click .environment-widget__expand")
  onExpandClick() {
    this.listLimit = this.model.get("items").length;
    this.render();
  }

  serializeData() {
    const items = this.model.get("items");
    return {
      items: items.slice(0, this.listLimit),
      overLimit: items.length > this.listLimit,
    };
  }
}
