import { View } from "backbone.marionette";
import { className } from "../../decorators/index";
import template from "./CategoryView.hbs";

@className("pane__section")
class CategoryView extends View {
  template = template;

  serializeData() {
    const extra = this.model.get("extra");
    return {
      categories: extra ? extra.categories : null,
    };
  }
}

export default CategoryView;
