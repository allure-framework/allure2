import { Model } from "backbone";
import { View } from "backbone.marionette";
import $ from "jquery";
import "./styles.scss";
import { className, on } from "../../decorators";
import router from "../../router";
import { makeArray } from "../../utils/arrays";
import AttachmentView from "../attachment/AttachmentView";
import template from "./TestResultExecutionView.hbs";

@className("test-result-execution")
class TestResultExecutionView extends View {
  template = template;

  initialize() {
    this.state = new Model();
    this.routeState = this.options.routeState;
    this.listenTo(this.state, "change:attachment", this.highlightSelectedAttachment, this);
  }

  onRender() {
    const attachment = this.routeState.get("attachment");
    if (attachment) {
      this.highlightSelectedAttachment(attachment);
    }
  }

  highlightSelectedAttachment(currentAttachment) {
    this.$(".attachment-row").removeClass("attachment-row_selected");

    const attachmentEl = this.$(`.attachment-row[data-uid="${currentAttachment}"]`);
    attachmentEl.addClass("attachment-row_selected");
    attachmentEl.parents(".step").addClass("step_expanded");
  }

  serializeData() {
    const before = makeArray(this.model.get("beforeStages"));
    const test = makeArray(this.model.get("testStage"));
    const after = makeArray(this.model.get("afterStages"));
    return {
      hasContent: before.length + test.length + after.length > 0,
      before: before,
      test: test,
      after: after,
      baseUrl: this.options.baseUrl,
    };
  }

  @on("click .step__title_hasContent")
  onStepClick(e) {
    this.$(e.currentTarget)
      .parent()
      .toggleClass("step_expanded");
  }

  @on("click .attachment-row")
  onAttachmentClick(e) {
    const attachmentUid = $(e.currentTarget).data("uid");
    const name = `attachment__${attachmentUid}`;

    if ($(e.currentTarget).hasClass("attachment-row_selected") && this.getRegion(name)) {
      this.getRegion(name).destroy();
    } else {
      this.addRegion(name, { el: this.$(`.${name}`) });
      this.getRegion(name).show(
        new AttachmentView({
          attachment: this.model.getAttachment(attachmentUid),
        }),
      );
    }
    this.$(e.currentTarget).toggleClass("attachment-row_selected");
  }

  @on("click .attachment-row__fullscreen")
  onAttachmnetFullScrennClick(e) {
    const attachment = $(e.currentTarget)
      .closest(".attachment-row")
      .data("uid");
    router.setSearch({ attachment });
    e.stopPropagation();
  }

  @on("click .attachment-row__link")
  onAttachmentFileClick(e) {
    e.stopPropagation();
  }

  @on("click .parameters-table__cell")
  onParameterClick(e) {
    this.$(e.target)
      .siblings()
      .addBack()
      .toggleClass("line-ellipsis");
  }
}

export default TestResultExecutionView;
