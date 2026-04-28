import "./AppLayout.scss";
import BaseElement from "../../core/elements/BaseElement.mts";
import { reportDataUrl } from "../../core/services/reportData.mts";
import { createReportLoadErrorView } from "../../core/view/asyncMount.mts";
import type { Mountable } from "../../core/view/types.mts";
import b from "../../shared/bem/index.mts";
import { createElement } from "../../shared/dom.mts";
import LoaderView from "../../shared/ui/LoaderView.mts";
import TooltipView from "../../shared/ui/TooltipView.mts";
import gtag from "../../utils/gtag.mts";
import SideNavElement from "./SideNavElement.mts";

type RouteArguments = import("../../core/registry/types.mts").RouteArguments;

export type AppLayoutOptions = Record<string, unknown> & {
  contentScrollable?: boolean;
  createContentView: () => Mountable;
  loadData?: () => Promise<unknown> | unknown;
  onViewReady?: (...args: RouteArguments) => void;
  onRouteUpdate?: (...args: RouteArguments) => void;
  shouldKeepState?: (...args: RouteArguments) => boolean;
};

class AppLayoutElement extends BaseElement {
  declare options: AppLayoutOptions;

  declare downloadTooltip: TooltipView;

  declare loadRequestId: number;

  constructor() {
    super();
    this.downloadTooltip = new TooltipView({
      position: "left",
    });
    this.loadRequestId = 0;
  }

  loadData() {
    return this.options.loadData?.();
  }

  createContentView() {
    return this.options.createContentView();
  }

  onViewReady(...args: RouteArguments) {
    return this.options.onViewReady?.(...args);
  }

  onRouteUpdate(...args: RouteArguments) {
    return this.options.onRouteUpdate?.(...args);
  }

  shouldKeepState(...args: RouteArguments) {
    if (!this.options.onRouteUpdate) {
      return false;
    }

    return this.options.shouldKeepState ? this.options.shouldKeepState(...args) : true;
  }

  renderElement() {
    const requestId = ++this.loadRequestId;

    this.className = "app";
    this.replaceChildren(
      createElement("div", {
        className: b("app", "nav"),
      }),
      createElement("div", {
        className: b("app", "content", {
          "no-scroll": this.options.contentScrollable === false,
        }),
      }),
    );
    this.bindEvents(
      {
        "click [data-ga4-event]": "onGaEventClick",
        "click [data-download]": "onDownloadableClick",
      },
      this,
    );
    this.mountChild("nav", SideNavElement(), ".app__nav");

    if (!this.options.loadData) {
      this.mountChild("content", this.createContentView(), ".app__content");
      return this;
    }

    this.mountChild("content", LoaderView({ variant: "screen" }), ".app__content");

    let dataPromise: Promise<unknown> | unknown;

    try {
      dataPromise = this.loadData();
    } catch (error: unknown) {
      this.mountChild("content", createReportLoadErrorView(error), ".app__content");
      return this;
    }

    if (!dataPromise) {
      this.mountChild("content", this.createContentView(), ".app__content");
      this.onViewReady();
      return this;
    }

    Promise.resolve(dataPromise)
      .then(() => {
        if (requestId !== this.loadRequestId || !this.isConnected) {
          return;
        }

        this.mountChild("content", this.createContentView(), ".app__content");
        this.onViewReady();
      })
      .catch((error: unknown) => {
        if (requestId !== this.loadRequestId || !this.isConnected) {
          return;
        }

        this.mountChild("content", createReportLoadErrorView(error), ".app__content");
      });

    return this;
  }

  onGaEventClick(e: Event) {
    const element = e.currentTarget as HTMLElement;
    const event = element.dataset.ga4Event;
    if (!event) {
      return;
    }
    const dataAttributes = element.dataset;
    const eventParams = Object.keys(dataAttributes)
      .filter((key) => key.startsWith("ga4Param"))
      .map((key) => {
        const value = dataAttributes[key];
        const gaKey = key
          .substring(8)
          .split(/\.?(?=[A-Z])/)
          .join("_")
          .toLowerCase();

        return {
          [gaKey]: value,
        };
      })
      .reduce((left, right) => Object.assign(left, right), {});

    gtag(event, eventParams);
  }

  onDownloadableClick(e: Event) {
    e.preventDefault();
    e.stopPropagation();

    const element = e.currentTarget as HTMLElement;
    const path = element.dataset.download;
    if (!path) {
      return;
    }

    const contentType = element.dataset.downloadType || "application/octet-stream";
    const target = element.dataset.downloadTarget === "_blank";

    reportDataUrl(path, contentType)
      .then((href) => {
        const link = document.createElement("a");
        link.setAttribute("href", href);
        link.setAttribute("download", path);
        if (target) {
          link.setAttribute("target", "_blank");
        }

        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      })
      .catch((error: unknown) => {
        this.downloadTooltip.show(
          `Download error: ${error instanceof Error ? error.message : String(error)}`,
          element,
        );
      });
  }

  destroy() {
    this.loadRequestId += 1;
    this.downloadTooltip.hide();
    super.destroy();
  }
}

if (!customElements.get("allure-app-layout")) {
  customElements.define("allure-app-layout", AppLayoutElement);
}

const createAppLayout = (options?: AppLayoutOptions) => {
  const element = document.createElement("allure-app-layout") as AppLayoutElement;
  element.setOptions(options);
  return element;
};

export default createAppLayout;
