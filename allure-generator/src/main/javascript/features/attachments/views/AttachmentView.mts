import BaseElement from "../../../core/elements/BaseElement.mts";
import router from "../../../core/routing/router.mts";
import attachmentType from "../model/attachmentType.mts";
import { PreviewView } from "./PreviewView.mts";

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

  declare suppressRouteReset?: boolean;

  constructor() {
    super();
    this.fullScreen = false;
  }

  setOptions(options: AttachmentViewOptions) {
    super.setOptions(options);
    this.fullScreen = !!options.fullScreen;
    this.attachment = options.attachment;
    this.attachmentInfo = attachmentType(this.attachment.type);
    return this;
  }

  renderElement() {
    this.className = "attachment";
    this.replaceChildren();
    this.mountChild(
      "preview",
      PreviewView({
        view: this.attachmentInfo.view,
        attachment: this.attachment,
        fullScreen: this.fullScreen,
      }),
      this,
    );
    return this;
  }

  destroy() {
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
