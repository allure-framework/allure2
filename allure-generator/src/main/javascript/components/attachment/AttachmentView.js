import "./styles.scss";
import { View } from "backbone.marionette";
import $ from "jquery";
import { behavior, className, on, regions } from "../../decorators";
import router from "../../router";
import attachmentType from "../../utils/attachmentType";
import highlight from "../../utils/highlight";
import template from "./AttachmentView.hbs";

@className("attachment")
@behavior("TooltipBehavior", { position: "bottom" })
@regions({
  customView: ".attachment__custom-view",
})
class AttachmentView extends View {
  template = template;

  initialize() {
    this.fullScreen = !!this.options.fullScreen;
    this.attachment = this.options.attachment;
    this.attachmentInfo = attachmentType(this.attachment.type);
    this.sourceUrl = `data/attachments/${this.attachment.source}`;
  }

  onRender() {
    if (this.attachmentInfo.type === "custom") {
      this.showChildView(
        "customView",
        new this.attachmentInfo.View({
          sourceUrl: this.sourceUrl,
          attachment: this.attachment,
        }),
      );
    } else if (this.needsFetch() && !this.content) {
      this.loadContent().then(this.render);
    } else if (this.attachmentInfo.type === "code") {
      const codeBlock = this.$(".attachment__code");
      codeBlock.addClass(`language-${this.attachment.type.split("/").pop()}`);
      highlight.highlightBlock(codeBlock[0]);
    }
  }

  onDestroy() {
    router.setSearch({ attachment: null });
  }

  @on("click .attachment__media-container")
  onImageClick(e) {
    const el = this.$(e.currentTarget);
    if (el.hasClass("attachment__media-container_fullscreen")) {
      this.onDestroy();
    } else {
      router.setSearch({ attachment: this.attachment.uid });
    }
  }

  loadContent() {
    return $.ajax(this.sourceUrl, { dataType: "text" }).then((responseText) => {
      const parser = this.attachmentInfo.parser;
      this.content = parser(responseText);
    });
  }

  needsFetch() {
    return "parser" in this.attachmentInfo;
  }

  serializeData() {
    return {
      type: this.attachmentInfo.type,
      content: this.content,
      sourceUrl: this.sourceUrl,
      attachment: this.attachment,
      fullScreen: this.fullScreen,
    };
  }
}

export default AttachmentView;
