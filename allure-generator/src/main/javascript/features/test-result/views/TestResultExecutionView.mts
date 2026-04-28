import "./TestResultExecutionView.scss";
import router from "../../../core/routing/router.mts";
import { bindDelegatedEvents } from "../../../core/view/domEvents.mts";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import {
  attachMountable,
  destroyMountable,
  resolveMountTarget,
} from "../../../core/view/mountables.mts";
import { makeArray } from "../../../utils/arrays.mts";
import { AttachmentView } from "../../attachments/runtime.mts";
import { getTestResultAttachment } from "../model/testResultData.mts";
import { createTestResultExecutionContent } from "./renderTestResultExecution.mts";

type TestResultExecutionOptions = {
  data: import("../../../types/report.mts").TestResult;
  attachmentsByUid: import("../model/testResultData.mts").TestResultAttachmentLookup;
  routeState: import("../../../core/state/StateStore.mts").default;
  baseUrl: string;
};

const createTestResultExecutionView = (options: TestResultExecutionOptions) => {
  const el = defineMountableElement(document.createElement("div"), {
    data: options.data,
    attachmentsByUid: options.attachmentsByUid,
    routeState: options.routeState,
    highlightSelectedAttachment(currentAttachment: string) {
      el.querySelectorAll(".attachment-row").forEach((row) =>
        row.classList.remove("attachment-row_selected"),
      );
      const attachmentEl = el.querySelector(`.attachment-row[data-uid="${currentAttachment}"]`);
      attachmentEl?.classList.add("attachment-row_selected");
      let step = attachmentEl?.closest(".step");
      while (step) {
        step.classList.add("step_expanded");
        step = step.parentElement?.closest(".step");
      }
    },
    onStepClick(event: Event) {
      (event.currentTarget as HTMLElement).parentElement?.classList.toggle("step_expanded");
    },
    onAttachmentClick(event: Event) {
      const currentTarget = event.currentTarget as HTMLElement;
      if (event.target instanceof Element && event.target.closest(".attachment-row__fullscreen")) {
        return;
      }
      if (currentTarget.dataset.viewer === "playwright-trace") {
        const traceAttachmentUid = currentTarget.dataset.uid;
        if (traceAttachmentUid) {
          router.setSearch({
            attachment: traceAttachmentUid,
          });
        }
        return;
      }
      const attachmentUid = currentTarget.dataset.uid;
      if (!attachmentUid) {
        return;
      }
      const attachment = getTestResultAttachment(options.attachmentsByUid, attachmentUid);
      if (!attachment) {
        return;
      }
      const name = `attachment__${attachmentUid}`;
      if (currentTarget.classList.contains("attachment-row_selected") && getMountedChild(name)) {
        unmountChild(name);
      } else {
        mountChild(
          name,
          AttachmentView({
            attachment,
          }),
          el.querySelector(`.${name}`),
        );
      }
      currentTarget.classList.toggle("attachment-row_selected");
    },
    onAttachmnetFullScrennClick(event: Event) {
      const currentTarget = event.currentTarget as HTMLElement;
      const attachment = currentTarget.closest(".attachment-row")?.getAttribute("data-uid");
      event.stopImmediatePropagation?.();
      event.stopPropagation();
      router.setSearch({
        attachment,
      });
    },
    onParameterClick(event: Event) {
      const target = event.target;
      if (!(target instanceof Element) || !target.parentElement) {
        return;
      }

      Array.from(target.parentElement.children).forEach((element) => {
        element.classList.toggle("line-ellipsis");
      });
    },
  });
  const mountedChildren = new Map<
    string,
    { container: Element; mountable: import("../../../core/view/types.mts").Mountable }
  >();
  let releaseEvents = () => {};

  const unmountChild = (name: string) => {
    const mounted = mountedChildren.get(name);
    if (!mounted) {
      return;
    }

    destroyMountable(mounted.mountable);
    mounted.container.replaceChildren();
    mountedChildren.delete(name);
  };

  const destroyMountedChildren = () => {
    Array.from(mountedChildren.keys()).forEach(unmountChild);
  };

  const mountChild = (
    name: string,
    childMountable: import("../../../core/view/types.mts").Mountable,
    target: import("../../../core/view/types.mts").MountTarget,
  ) => {
    const container = resolveMountTarget(el, target);
    if (!container) {
      throw new Error(`Mount target for "${name}" is not attached to the DOM`);
    }

    unmountChild(name);
    mountedChildren.set(name, { container, mountable: childMountable });
    attachMountable(container, childMountable);
    return childMountable;
  };

  const getMountedChild = (name: string) => mountedChildren.get(name)?.mountable || null;

  Object.assign(el, {
    render() {
      destroyMountedChildren();
      releaseEvents();
      const before = makeArray(options.data.beforeStages);
      const test = makeArray(options.data.testStage);
      const after = makeArray(options.data.afterStages);
      el.className = "test-result-execution";
      el.replaceChildren(
        createTestResultExecutionContent({
          hasContent: before.length + test.length + after.length > 0,
          before,
          test,
          after,
        }),
      );
      releaseEvents = bindDelegatedEvents({
        root: el,
        events: {
          "click .step__title_hasContent": "onStepClick",
          "click .attachment-row__fullscreen": "onAttachmnetFullScrennClick",
          "click .attachment-row": "onAttachmentClick",
          "click .parameters-table__cell": "onParameterClick",
        },
        context: el,
      });
      const attachment = options.routeState.get("attachment");
      if (typeof attachment === "string" && attachment) {
        el.highlightSelectedAttachment(attachment);
      }
      return el;
    },
    attachToDom() {
      mountedChildren.forEach(({ mountable: childMountable }) => childMountable.attachToDom?.());
    },
    detachFromDom() {
      mountedChildren.forEach(({ mountable: childMountable }) => childMountable.detachFromDom?.());
    },
    destroy() {
      el.detachFromDom?.();
      destroyMountedChildren();
      releaseEvents();
      el.remove();
    },
  });

  return el;
};

export default createTestResultExecutionView;
