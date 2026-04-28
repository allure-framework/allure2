import "./AttachmentView.scss";
import BaseElement from "../../../core/elements/BaseElement.mts";
import router from "../../../core/routing/router.mts";
import {
  fetchReportText,
  normalizeReportDataError,
  type ReportDataError,
  reportDataUrl,
} from "../../../core/services/reportData.mts";
import translate from "../../../helpers/t.mts";
import { createElement } from "../../../shared/dom.mts";
import { escapeHtml } from "../../../shared/html.mts";
import ErrorSplashView from "../../../shared/ui/ErrorSplashView.mts";
import LoaderView from "../../../shared/ui/LoaderView.mts";
import TooltipView from "../../../shared/ui/TooltipView.mts";
import highlight from "../../../utils/highlight.mts";
import attachmentType from "../model/attachmentType.mts";
import { PlaywrightTraceAttachmentView } from "./PlaywrightTraceAttachmentView.mts";
import { renderAttachmentView } from "./renderAttachmentView.mts";

type Attachment = import("../../../types/report.mts").Attachment;
type AttachmentTypeInfo = import("../model/attachmentType.mts").AttachmentTypeInfo;

type AttachmentViewOptions = {
  attachment: Attachment;
  fullScreen?: boolean;
} & Record<string, unknown>;

class AttachmentElement extends BaseElement {
  declare options: AttachmentViewOptions;

  declare fullScreen: boolean;

  declare attachment: Attachment;

  declare attachmentInfo: AttachmentTypeInfo;

  declare tooltip: TooltipView;

  declare sourceUrl: string | null;

  declare content?: unknown;

  declare isLoading: boolean;

  declare loadError: ReportDataError | null;

  declare suppressRouteReset?: boolean;

  declare renderRequestId: number;

  constructor() {
    super();
    this.fullScreen = false;
    this.isLoading = false;
    this.loadError = null;
    this.sourceUrl = null;
    this.renderRequestId = 0;
    this.tooltip = new TooltipView({
      position: "bottom",
    });
  }

  setOptions(options: AttachmentViewOptions) {
    const shouldResetContent =
      this.attachment?.uid !== options.attachment.uid ||
      this.attachment?.source !== options.attachment.source ||
      this.attachment?.type !== options.attachment.type;

    if (shouldResetContent && this.sourceUrl?.startsWith?.("blob:")) {
      URL.revokeObjectURL(this.sourceUrl);
    }

    super.setOptions(options);
    this.fullScreen = !!options.fullScreen;
    this.attachment = options.attachment;
    this.attachmentInfo = attachmentType(this.attachment.type);
    if (shouldResetContent) {
      this.sourceUrl = null;
      this.content = undefined;
      this.isLoading = false;
      this.loadError = null;
    }
    return this;
  }

  renderElement() {
    const requestId = ++this.renderRequestId;
    const isContentPending =
      this.needsFetch() &&
      typeof this.content === "undefined" &&
      !!this.sourceUrl &&
      !this.loadError;

    if (this.isLoading || isContentPending) {
      this.className = "attachment";
      this.replaceChildren(createElement("div", { className: "attachment__state" }));
      this.mountChild("state", LoaderView(), ".attachment__state");
      return this;
    }

    if (this.loadError) {
      this.className = "attachment";
      this.replaceChildren(createElement("div", { className: "attachment__state" }));
      this.mountChild(
        "state",
        ErrorSplashView({
          code: this.loadError.status,
          message: this.loadError.message,
        }),
        ".attachment__state",
      );
      return this;
    }

    if (this.shouldLoadAttachment()) {
      this.className = "attachment";
      this.replaceChildren(createElement("div", { className: "attachment__state" }));
      this.mountChild("state", LoaderView(), ".attachment__state");
      this.loadAttachment(requestId);
      return this;
    }

    this.className = "attachment";
    this.replaceChildren(
      renderAttachmentView({
        type: this.attachmentInfo.type,
        content: this.content,
        sourceUrl: this.sourceUrl || undefined,
        attachment: this.attachment,
        fullScreen: this.fullScreen,
      }),
    );
    this.bindEvents(
      {
        "click .attachment__media-container": "onImageClick",
        "mouseenter [data-tooltip]": "onTooltipHover",
        "mouseleave [data-tooltip]": "onTooltipLeave",
      },
      this,
    );

    if (this.attachmentInfo.type === "playwright-trace" && this.sourceUrl) {
      this.mountChild(
        "playwrightTraceView",
        PlaywrightTraceAttachmentView({
          sourceUrl: this.sourceUrl,
          attachment: this.attachment,
        }),
        ".attachment__trace-view",
      );
    } else if (
      this.attachmentInfo.type === "custom" &&
      this.attachmentInfo.create &&
      this.sourceUrl
    ) {
      this.mountChild(
        "customView",
        this.attachmentInfo.create({
          sourceUrl: this.sourceUrl,
          attachment: this.attachment,
        }),
        ".attachment__custom-view",
      );
    } else if (this.attachmentInfo.type === "code") {
      const codeBlock = this.querySelector(".attachment__code") as HTMLElement | null;
      codeBlock?.classList.add(`language-${this.attachment.type.split("/").pop()}`);
      if (codeBlock) {
        highlight.highlightElement(codeBlock);
      }
    }

    return this;
  }

  isCurrentRequest(requestId: number) {
    return requestId === this.renderRequestId;
  }

  shouldLoadAttachment() {
    if (this.isLoading || this.loadError) {
      return false;
    }

    if (!this.sourceUrl) {
      return true;
    }

    return this.needsFetch() && this.content === undefined;
  }

  async loadAttachment(requestId: number) {
    this.isLoading = true;
    this.loadError = null;

    try {
      if (this.attachmentInfo.type === "svg") {
        const svgText = await fetchReportText(`data/attachments/${this.attachment.source}`, {
          contentType: this.attachment.type,
        });
        if (!this.isCurrentRequest(requestId)) {
          return;
        }

        const blob = new Blob([svgText], {
          type: this.attachment.type || "image/svg+xml",
        });
        this.sourceUrl = URL.createObjectURL(blob);
      } else {
        if (!this.sourceUrl) {
          const sourceUrl = await reportDataUrl(
            `data/attachments/${this.attachment.source}`,
            this.attachment.type,
          );
          if (!this.isCurrentRequest(requestId)) {
            return;
          }

          this.sourceUrl = sourceUrl;
        }

        if (this.needsFetch() && this.content === undefined) {
          await this.loadContent(requestId);
        }
      }

      if (!this.isCurrentRequest(requestId)) {
        return;
      }

      this.isLoading = false;
      this.loadError = null;
      if (this.isConnected) {
        this.render();
      }
    } catch (error: unknown) {
      if (!this.isCurrentRequest(requestId)) {
        return;
      }

      this.isLoading = false;
      this.loadError = normalizeReportDataError(error, {
        message: translate("errors.loadingFailed"),
        url: `data/attachments/${this.attachment.source}`,
      });
      if (this.isConnected) {
        this.render();
      }
    }
  }

  onImageClick(e: Event) {
    const element = e.currentTarget as HTMLElement;
    if (element.classList.contains("attachment__media-container_fullscreen")) {
      router.setSearch({
        attachment: null,
      });
    } else {
      router.setSearch({
        attachment: this.attachment.uid,
      });
    }
  }

  needsFetch() {
    return "parser" in this.attachmentInfo;
  }

  loadContent(requestId: number) {
    if (!this.sourceUrl) {
      throw new Error("Attachment source URL is required");
    }

    return fetchReportText(this.sourceUrl, {
      contentType: this.attachment.type,
    }).then((responseText) => {
      if (!this.isCurrentRequest(requestId)) {
        return;
      }

      const parser = this.attachmentInfo.parser;
      this.content = parser?.(responseText);
    });
  }

  onTooltipHover(e: Event) {
    const element = e.currentTarget as HTMLElement;
    this.tooltip.show(escapeHtml(element.dataset.tooltip || ""), element);
  }

  onTooltipLeave() {
    this.tooltip.hide();
  }

  destroy() {
    this.renderRequestId += 1;
    this.tooltip.hide();

    if (this.sourceUrl?.startsWith?.("blob:")) {
      URL.revokeObjectURL(this.sourceUrl);
    }
    if (this.fullScreen && !this.suppressRouteReset) {
      router.setSearch({
        attachment: null,
      });
    }

    super.destroy();
  }
}

if (!customElements.get("allure-attachment")) {
  customElements.define("allure-attachment", AttachmentElement);
}

const createAttachmentView = (options: AttachmentViewOptions) => {
  const element = document.createElement("allure-attachment") as AttachmentElement;
  element.setOptions(options);
  return element;
};

export default createAttachmentView;
