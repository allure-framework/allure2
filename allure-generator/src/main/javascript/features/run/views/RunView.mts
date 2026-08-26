import "./RunView.scss";
import BaseElement from "../../../core/elements/BaseElement.mts";
import date from "../../../helpers/date.mts";
import translate from "../../../helpers/t.mts";
import time from "../../../helpers/time.mts";
import { escapeHtml } from "../../../shared/html.mts";
import { createElement } from "../../../shared/dom.mts";
import { createIconElement } from "../../../shared/icon/index.mts";
import ModalView from "../../../shared/ui/ModalView.mts";
import TooltipView from "../../../shared/ui/TooltipView.mts";
import { AttachmentView } from "../../attachments/runtime.mts";
import { createAttachmentRow } from "../../attachments/views/renderAttachmentRow.mts";

type GlobalAttachment = import("../../../types/report.mts").GlobalAttachment;
type GlobalError = import("../../../types/report.mts").GlobalError;
type GlobalsData = import("../../../types/report.mts").GlobalsData;

type RunViewOptions = {
  data: GlobalsData;
};

const attachmentUid = (attachment: GlobalAttachment) => String(attachment.uid || attachment.source);

const createErrorCard = (error: GlobalError, index: number) => {
  const hasComparison = typeof error.actual === "string" || typeof error.expected === "string";
  const hasTimestamp = typeof error.timestamp === "number";

  return createElement("article", {
    className: "run-view__error island",
    children: [
      createElement("div", {
        className: "run-view__error-head",
        children: [
          createElement("div", {
            className: "run-view__error-index",
            text: index + 1,
          }),
          createElement("h3", {
            className: "run-view__error-message",
            text: error.message || translate("run.unknownError"),
          }),
        ],
      }),
      hasTimestamp
        ? createElement("div", {
            className: "run-view__timestamp",
            text: translate("testResult.history.time", {
              hash: { date: date(error.timestamp), time: time(error.timestamp, true) },
            }),
          })
        : null,
      hasComparison
        ? createElement("dl", {
            className: "run-view__comparison",
            children: [
              typeof error.expected === "string"
                ? [
                    createElement("dt", { text: translate("run.expected") }),
                    createElement("dd", { text: error.expected }),
                  ]
                : null,
              typeof error.actual === "string"
                ? [
                    createElement("dt", { text: translate("run.actual") }),
                    createElement("dd", { text: error.actual }),
                  ]
                : null,
            ],
          })
        : null,
      error.trace
        ? createElement("details", {
            className: "run-view__trace",
            children: [
              createElement("summary", { text: translate("run.trace") }),
              createElement("pre", {
                children: createElement("code", { text: error.trace }),
              }),
            ],
          })
        : null,
    ],
  });
};

class RunViewElement extends BaseElement {
  declare options: RunViewOptions;

  declare data: GlobalsData;

  declare tooltip: TooltipView;

  declare modalView: ReturnType<typeof ModalView> | null;

  constructor() {
    super();
    this.data = { errors: [], attachments: [] };
    this.tooltip = new TooltipView({ position: "left" });
    this.modalView = null;
  }

  setOptions(options: RunViewOptions) {
    super.setOptions(options);
    this.data = options.data;
    return this;
  }

  renderElement() {
    const errorCount = this.data.errors.length;
    const failed = errorCount > 0;

    this.className = "run-view";
    this.replaceChildren(
      createElement("h1", {
        className: "pane__title",
        text: translate("tab.run.name"),
      }),
      createElement("div", {
        className: `run-view__outcome island run-view__outcome_${failed ? "failed" : "clean"}`,
        children: [
          createElement("div", {
            className: "run-view__outcome-icon",
            children: createIconElement(failed ? "solidAlertCircle" : "lineGeneralInfoCircle", {
              size: "l",
            }),
          }),
          createElement("div", {
            children: [
              createElement("h2", {
                className: "run-view__outcome-title",
                text: translate(failed ? "run.failed" : "run.noErrors"),
              }),
              createElement("p", {
                className: "run-view__outcome-description",
                text: translate(failed ? "run.failureExplanation" : "run.noErrorsExplanation"),
              }),
            ],
          }),
        ],
      }),
      this.createErrorsSection(),
      this.createAttachmentsSection(),
    );
    this.bindEvents(
      {
        "click .attachment-row__fullscreen": "onAttachmentFullscreenClick",
        "click .attachment-row": "onAttachmentClick",
        "mouseenter [data-tooltip]": "onTooltipHover",
        "mouseleave [data-tooltip]": "onTooltipLeave",
      },
      this,
    );

    return this;
  }

  createErrorsSection() {
    const errors = this.data.errors;
    return createElement("section", {
      className: "run-view__section",
      children: [
        createElement("h2", {
          className: "run-view__section-title",
          children: [
            translate("run.errors"),
            createElement("span", {
              className: "run-view__count",
              text: errors.length,
            }),
          ],
        }),
        errors.length
          ? createElement("div", {
              className: "run-view__errors",
              children: errors.map(createErrorCard),
            })
          : createElement("p", {
              className: "run-view__empty",
              text: translate("run.noErrors"),
            }),
      ],
    });
  }

  createAttachmentsSection() {
    const attachments = this.data.attachments;
    return createElement("section", {
      className: "run-view__section",
      children: [
        createElement("h2", {
          className: "run-view__section-title",
          children: [
            translate("run.attachments"),
            createElement("span", {
              className: "run-view__count",
              text: attachments.length,
            }),
          ],
        }),
        attachments.length
          ? createElement("div", {
              className: "run-view__attachments island",
              children: attachments.map((attachment) => {
                const normalized = { ...attachment, uid: attachmentUid(attachment) };
                return createElement("div", {
                  className: "run-view__attachment",
                  children: createAttachmentRow(normalized),
                });
              }),
            })
          : createElement("p", {
              className: "run-view__empty",
              text: translate("run.noAttachments"),
            }),
      ],
    });
  }

  findAttachment(uid: string) {
    return this.data.attachments.find((attachment) => attachmentUid(attachment) === uid);
  }

  onAttachmentClick(event: Event) {
    const row = event.currentTarget as HTMLElement;
    const target = event.target;
    if (
      target instanceof Element &&
      target.closest("[data-download], .attachment-row__fullscreen")
    ) {
      return;
    }

    const uid = row.dataset.uid;
    if (!uid) {
      return;
    }
    const attachment = this.findAttachment(uid);
    if (!attachment) {
      return;
    }
    if (row.dataset.viewer === "playwright-trace") {
      this.showAttachmentModal(attachment);
      return;
    }

    const name = `attachment__${uid}`;
    if (row.classList.contains("attachment-row_selected") && this.getMountedChild(name)) {
      this.unmountChild(name);
    } else {
      this.mountChild(
        name,
        AttachmentView({ attachment: { ...attachment, uid } }),
        this.querySelector(`.${name}`),
      );
    }
    row.classList.toggle("attachment-row_selected");
  }

  onAttachmentFullscreenClick(event: Event) {
    event.preventDefault();
    event.stopPropagation();
    const uid = (event.currentTarget as Element)
      .closest(".attachment-row")
      ?.getAttribute("data-uid");
    const attachment = uid ? this.findAttachment(uid) : undefined;
    if (attachment) {
      this.showAttachmentModal(attachment);
    }
  }

  showAttachmentModal(attachment: GlobalAttachment) {
    this.modalView?.destroy();
    const childView = AttachmentView({
      attachment: { ...attachment, uid: attachmentUid(attachment) },
      fullScreen: true,
    });
    childView.suppressRouteReset = true;
    this.modalView = ModalView({
      childView,
      title: attachment.name || attachment.source,
    });
    this.modalView.show();
  }

  onTooltipHover(event: Event) {
    const element = event.currentTarget as HTMLElement;
    this.tooltip.show(escapeHtml(element.dataset.tooltip || ""), element);
  }

  onTooltipLeave() {
    this.tooltip.hide();
  }

  destroy() {
    this.tooltip.hide();
    this.modalView?.destroy();
    this.modalView = null;
    super.destroy();
  }
}

if (!customElements.get("allure-run-view")) {
  customElements.define("allure-run-view", RunViewElement);
}

const RunView = (options: RunViewOptions) => {
  const element = document.createElement("allure-run-view") as RunViewElement;
  element.setOptions(options);
  return element;
};

export default RunView;
