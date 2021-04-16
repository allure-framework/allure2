import { Model } from "backbone";

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

  getWidgetData(name) {
    const items = this.get(name);
    return new Model(Array.isArray(items) ? { items } : items);
  }
}
