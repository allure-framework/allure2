import { Model } from "backbone";
import { reportDataUrl } from "../loader";

export default class WidgetsModel extends Model {
  initialize(model, options) {
    this.options = options;
  }

  url() {
    return `widgets/${this.options.name}.json`;
  }

  parse(data) {
    return Array.isArray(data) ? { items: data } : data;
  }

  fetch(options) {
    return reportDataUrl(this.url(), "application/json").then((value) =>
      super.fetch({ ...options, url: value }),
    );
  }

  getWidgetData(name) {
    const items = this.get(name);
    return new Model(Array.isArray(items) ? { items } : items);
  }
}
