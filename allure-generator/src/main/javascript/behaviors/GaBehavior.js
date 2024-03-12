import { Behavior } from "backbone.marionette";
import { on } from "../decorators";
import gtag from "../utils/gtag";

export default class GaBehavior extends Behavior {
  initialize() {}

  @on("click [data-ga4-event]")
  onDataEventClick(e) {
    const el = this.$(e.currentTarget);
    const event = el.data("ga4-event");
    const dataAttributes = el.data();
    const eventParams = Object.keys(dataAttributes)
      .filter((key) => key.startsWith("ga4Param"))
      .map((key) => {
        const value = dataAttributes[key];
        const gaKey = key
          .substring(8)
          .split(/\.?(?=[A-Z])/)
          .join("_")
          .toLowerCase();
        return { [gaKey]: value };
      })
      .reduce((a, b) => Object.assign(a, b), {});
    gtag(event, eventParams);
  }
}
