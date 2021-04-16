import "./styles.scss";
import { View } from "backbone.marionette";
import { className, on } from "../../decorators";
import template from "./NodeSorterView.hbs";

const AVAILABLE_SORTERS = ["sorter.order", "sorter.name", "sorter.duration", "sorter.status"];

@className("sorter")
class NodeSorterView extends View {
  template = template;

  initialize({ settings }) {
    this.settings = settings;
  }

  @on("click .sorter__item")
  onChangeSorting(e) {
    const el = this.$(e.currentTarget);
    this.settings.setTreeSorting({
      sorter: el.data("name"),
      ascending: !el.data("asc"),
    });

    const ascending = el.data("asc");
    this.$(".sorter_enabled").toggleClass("sorter_enabled");
    el.data("asc", !ascending);
    el.find(".sorter__name").toggleClass("sorter_enabled");
    el.find(ascending ? ".fa-sort-asc" : ".fa-sort-desc").toggleClass("sorter_enabled");
  }

  serializeData() {
    const sortSettings = this.settings.getTreeSorting();
    return {
      sorters: AVAILABLE_SORTERS.map((sorter) => ({
        name: sorter,
        asc: sortSettings.sorter === sorter && sortSettings.ascending,
        desc: sortSettings.sorter === sorter && !sortSettings.ascending,
      })),
    };
  }
}

export default NodeSorterView;
