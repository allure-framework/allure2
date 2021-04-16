import "./styles.scss";
import { View } from "backbone.marionette";
import { findWhere } from "underscore";
import { behavior, className, on, regions } from "../../decorators";
import translate from "../../helpers/t";
import pluginsRegistry from "../../utils/pluginsRegistry";
import AttachmentView from "../attachment/AttachmentView";
import ErrorSplashView from "../error-splash/ErrorSplashView";
import ModalView from "../modal/ModalView";
import TestResultOverviewView from "../testresult-overview/TestResultOverviewView";
import template from "./TestResultView.hbs";

const subViews = [{ id: "", name: "testResult.overview.name", View: TestResultOverviewView }];

@className("test-result")
@behavior("TooltipBehavior", { position: "left" })
@behavior("ClipboardBehavior")
@regions({
  content: ".test-result__content",
})
class TestResultView extends View {
  template = template;

  initialize({ routeState }) {
    this.routeState = routeState;
    this.tabs = subViews.concat(pluginsRegistry.testResultTabs);
    this.tabName = this.routeState.get("testResultTab") || "";
    this.listenTo(this.routeState, "change:testResultTab", (_, tabName) =>
      this.onTabChange(tabName),
    );
    this.listenTo(this.routeState, "change:attachment", (_, uid) => this.onShowAttachment(uid));
  }

  onRender() {
    const subView = findWhere(this.tabs, { id: this.tabName });
    this.showChildView(
      "content",
      !subView
        ? new ErrorSplashView({ code: 404, message: `Tab "${this.tabName}" not found` })
        : new subView.View(this.options),
    );

    const attachment = this.routeState.get("attachment");
    if (attachment) {
      this.onShowAttachment(attachment);
    }
  }

  onTabChange(tabName) {
    this.tabName = tabName || "";
    this.render();
  }

  onShowAttachment(uid) {
    if (!uid && this.modalView) {
      this.modalView.destroy();
    }

    if (uid) {
      const attachment = this.model.getAttachment(uid);
      this.modalView = new ModalView({
        childView: attachment
          ? new AttachmentView({ attachment, fullScreen: true })
          : new ErrorSplashView({ code: 404, message: translate("errors.missedAttachment") }),
        title: attachment ? attachment.name || attachment.source : translate("errors.notFound"),
      });
      this.modalView.show();
    }
  }

  templateContext() {
    const { baseUrl } = this.options;
    return {
      cls: this.className,
      statusName: `status.${this.model.get("status")}`,
      links: this.tabs.map((view) => {
        return {
          href: `${baseUrl}/${view.id}`,
          name: view.name,
          active: view.id === this.tabName,
        };
      }),
    };
  }

  @on("click .status-details__trace-toggle")
  onStacktraceClick(e) {
    this.$(e.currentTarget)
      .closest(".status-details")
      .toggleClass("status-details__expanded");
  }
}

export default TestResultView;
