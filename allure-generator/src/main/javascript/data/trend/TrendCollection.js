import { Collection } from "backbone";
import { flatten, keys, omit, uniq, values } from "underscore";
import { reportDataUrl } from "../loader";

export default class TrendCollection extends Collection {
  initialize(models, options) {
    this.options = options;
    this.url = `widgets/${this.options.name}.json`;
  }

  fetch(options) {
    return reportDataUrl(this.url, "application/json").then((value) =>
      super.fetch({ ...options, url: value }),
    );
  }

  parse(response) {
    return response.reverse().map((item, id) => {
      return {
        ...item,
        id,
        name: item.buildOrder ? `#${item.buildOrder}` : "",
        total: values(omit(item.data, "total")).reduce((prev, curr) => prev + curr, 0),
        data: omit(item.data, "total"),
      };
    });
  }

  keys() {
    return uniq(flatten(this.map((model) => keys(model.get("data")))));
  }

  sortedKeysByLastValue() {
    const allKeys = this.keys();
    const lastData = this.last().get("data");
    return allKeys.sort((a, b) => (lastData[b] || 0) - (lastData[a] || 0));
  }
}
