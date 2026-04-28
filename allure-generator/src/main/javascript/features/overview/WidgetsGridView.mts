import "./WidgetsGridView.scss";
import BaseElement from "../../core/elements/BaseElement.mts";
import { getWidgets } from "../../core/registry/index.mts";
import { getSettingsForWidgetGridPlugin } from "../../core/services/settings.mts";
import { createReportLoadErrorView } from "../../core/view/asyncMount.mts";
import { createElement } from "../../shared/dom.mts";
import LoaderView from "../../shared/ui/LoaderView.mts";

type WidgetsGridOptions = {
  tabName: string;
  settings?: ReturnType<typeof getSettingsForWidgetGridPlugin>;
};

const createWidgetShell = (id: string) =>
  createElement("div", {
    attrs: {
      "data-id": id,
      draggable: "false",
    },
    className: "widget island",
    children: [
      createElement("div", {
        className: "widget__handle",
        children: createElement("span", {
          className: "draggable-icon",
        }),
      }),
      createElement("div", {
        className: "widget__body",
      }),
    ],
  });

class WidgetsGridElement extends BaseElement {
  declare options: WidgetsGridOptions;

  declare widgets: ReturnType<typeof getWidgets>;

  declare settings: ReturnType<typeof getSettingsForWidgetGridPlugin>;

  declare draggedWidget: HTMLElement | null;

  declare renderRequestId: number;

  constructor() {
    super();
    this.draggedWidget = null;
    this.renderRequestId = 0;
    this.widgets = {};
    this.settings = getSettingsForWidgetGridPlugin("widgets");
  }

  setOptions(options: WidgetsGridOptions) {
    super.setOptions(options);
    this.widgets = getWidgets(options.tabName);
    this.settings = options.settings || getSettingsForWidgetGridPlugin(options.tabName);
    return this;
  }

  renderElement() {
    const requestId = ++this.renderRequestId;

    this.className = "widgets-grid";
    this.replaceChildren();

    this.bindEvents(
      {
        "mousedown .widget__handle": "onHandlePointerDown",
        "touchstart .widget__handle": "onHandlePointerDown",
        "dragstart .widget": "onWidgetDragStart",
        "dragend .widget": "onWidgetDragEnd",
        "dragover .widgets-grid__col": "onColumnDragOver",
        "drop .widgets-grid__col": "onColumnDrop",
      },
      this,
    );

    this.getWidgetsArrangement().forEach((widgetNames) => {
      const col = createElement("div", { className: "widgets-grid__col" });
      this.appendChild(col);

      widgetNames.forEach((widgetName) => {
        const widget = this.widgets[widgetName];
        this.addWidget(col, widgetName, widget.create, widget.load, requestId);
      });
    });

    return this;
  }

  getWidgetsArrangement() {
    const savedData = this.settings.getWidgetsArrangement() || [[], []];
    const storedWidgets = savedData.map((col) =>
      col.filter((widgetName) => this.widgets[widgetName]),
    );

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

  onHandlePointerDown(event: Event) {
    const widget = (event.currentTarget as Element).closest(".widget") as HTMLElement | null;
    if (!widget) {
      return;
    }

    widget.draggable = true;
  }

  onWidgetDragStart(event: DragEvent) {
    const widget = event.currentTarget as HTMLElement;
    if (!widget.draggable) {
      event.preventDefault();
      return;
    }

    this.draggedWidget = widget;
    widget.classList.add("widget_ghost");

    if (event.dataTransfer) {
      event.dataTransfer.effectAllowed = "move";
      event.dataTransfer.setData("text/plain", widget.dataset.id || "");
    }
  }

  onWidgetDragEnd(event: DragEvent) {
    const widget = event.currentTarget as HTMLElement;
    widget.classList.remove("widget_ghost");
    widget.draggable = false;
    this.draggedWidget = null;
    this.saveWidgetsArrangement();
  }

  onColumnDragOver(event: DragEvent) {
    if (!this.draggedWidget) {
      return;
    }

    event.preventDefault();
    const column = event.currentTarget as Element;
    const nextWidget = Array.from(column.querySelectorAll(".widget"))
      .filter((widget) => widget !== this.draggedWidget)
      .find((widget) => {
        const { top, height } = (widget as Element).getBoundingClientRect();
        return (event.clientY || 0) < top + height / 2;
      });

    if (nextWidget) {
      column.insertBefore(this.draggedWidget, nextWidget);
    } else {
      column.appendChild(this.draggedWidget);
    }
  }

  onColumnDrop(event: Event) {
    event.preventDefault();
    this.saveWidgetsArrangement();
  }

  saveWidgetsArrangement() {
    this.settings.setWidgetsArrangement(
      Array.from(this.querySelectorAll(".widgets-grid__col"))
        .map((colEl) =>
          Array.from(colEl.querySelectorAll(".widget")).map(
            (element) => (element as HTMLElement).dataset.id,
          ),
        )
        .map((col) =>
          col.filter((widgetName): widgetName is string => typeof widgetName === "string"),
        ),
    );
  }

  addWidget(
    col: Element,
    name: string,
    create: ReturnType<typeof getWidgets>[string]["create"],
    load: ReturnType<typeof getWidgets>[string]["load"],
    requestId: number,
  ) {
    const element = createWidgetShell(name);
    col.appendChild(element);
    const target = element.querySelector(".widget__body");

    this.mountChild(name, LoaderView(), target);

    Promise.resolve()
      .then(() => load())
      .then((data) => {
        if (requestId !== this.renderRequestId || !this.isConnected) {
          return;
        }

        this.mountChild(name, create({ data }), target);
      })
      .catch((error: unknown) => {
        if (requestId !== this.renderRequestId || !this.isConnected) {
          return;
        }

        this.mountChild(name, createReportLoadErrorView(error), target);
      });
  }
}

if (!customElements.get("allure-widgets-grid")) {
  customElements.define("allure-widgets-grid", WidgetsGridElement);
}

const createWidgetsGridView = (options: WidgetsGridOptions) => {
  const element = document.createElement("allure-widgets-grid") as WidgetsGridElement;
  element.setOptions(options);
  return element;
};

export default createWidgetsGridView;
