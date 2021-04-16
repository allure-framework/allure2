import "./styles.scss";
import { View } from "backbone.marionette";
import $ from "jquery";
import Sortable from "sortablejs";
import { className } from "../../decorators";
import { fetchAndShow } from "../../utils/loading";
import pluginsRegistry from "../../utils/pluginsRegistry";
import { getSettingsForWidgetGridPlugin } from "../../utils/settingsFactory";

const widgetTpl = (id) => `<div class="widget island" data-id="${id}">
    <div class="widget__handle">
        <span class="draggable-icon"></span>
    </div>
    <div class="widget__body"></div>
</div>`;
const colTpl = '<div class="widgets-grid__col"></div>';

@className("widgets-grid")
class WidgetsGridView extends View {
  template = () => "";

  initialize() {
    this.widgets = pluginsRegistry.widgets[this.options.tabName];
    this.settings = this.options.settings || getSettingsForWidgetGridPlugin(this.options.tabName);
  }

  onRender() {
    this.getWidgetsArrangement()
      .map((col) => {
        return col.map((widgetName) => [
          widgetName,
          this.widgets[widgetName].widget,
          this.widgets[widgetName].model,
        ]);
      })
      .forEach((widgetCol) => {
        const col = $(colTpl);
        this.$el.append(col);
        widgetCol.forEach(([name, Widget, Model]) => {
          this.addWidget(col, name, Widget, Model);
        });
      });
    this.$(".widgets-grid__col").each(
      (i, colEl) =>
        new Sortable(colEl, {
          group: "widgets",
          ghostClass: "widget_ghost",
          handle: ".widget__handle",
          onEnd: this.saveWidgetsArrangement.bind(this),
        }),
    );
  }

  getWidgetsArrangement() {
    const savedData = this.settings.getWidgetsArrangement();

    const storedWidgets = savedData.map((col) => {
      return col.filter((widgetName) => this.widgets[widgetName]);
    });
    Object.keys(this.widgets).forEach((widgetName) => {
      if (storedWidgets.every((col) => col.indexOf(widgetName) === -1)) {
        const freeColumn = storedWidgets.reduce((smallestCol, col) =>
          col.length < smallestCol.length ? col : smallestCol,
        );
        freeColumn.push(widgetName);
      }
    });
    return storedWidgets;
  }

  saveWidgetsArrangement() {
    this.settings.setWidgetsArrangement(
      this.$(".widgets-grid__col")
        .toArray()
        .map((colEl) => {
          return $(colEl)
            .find(".widget")
            .toArray()
            .map((el) => $(el).data("id"));
        }),
    );
  }

  addWidget(col, name, Widget, Model) {
    const el = $(widgetTpl(name));
    col.append(el);
    this.addRegion(name, { el: el.find(".widget__body") });
    const widget = new Model({}, { name });
    fetchAndShow(this, name, widget, new Widget({ model: widget }));
  }
}

export default WidgetsGridView;
