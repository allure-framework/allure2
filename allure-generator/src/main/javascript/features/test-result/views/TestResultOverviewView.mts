import "./TestResultOverviewView.scss";
import { getTestResultBlocks } from "../../../core/registry/index.mts";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import {
  appendMountable,
  attachMountable,
  destroyMountable,
} from "../../../core/view/mountables.mts";
import { createTestResultOverviewContent } from "./renderTestResultOverview.mts";
import TestResultExecutionView from "./TestResultExecutionView.mts";

type TestResultOverviewOptions = {
  data: import("../../../types/report.mts").TestResult;
  attachmentsByUid: import("../model/testResultData.mts").TestResultAttachmentLookup;
  routeState: import("../../../core/state/StateStore.mts").default;
  baseUrl: string;
};

const createTestResultOverviewView = (options: TestResultOverviewOptions) => {
  const el = defineMountableElement(document.createElement("div"), {
    data: options.data,
    showBlock(container: Element | null, blockFactories: ReturnType<typeof getTestResultBlocks>) {
      if (!container) {
        return;
      }

      blockFactories.forEach((create) => {
        const block = create({ data: options.data });
        blocks.push(block);
        appendMountable(container, block);
      });
    },
  });
  let blocks: import("../../../core/view/types.mts").Mountable[] = [];
  let execution: import("../../../core/view/types.mts").Mountable | null = null;

  const destroyBlocks = () => {
    blocks.forEach((block) => destroyMountable(block));
    blocks = [];
  };

  const destroyExecution = () => {
    if (!execution) {
      return;
    }

    destroyMountable(execution);
    execution = null;
  };

  const mountExecution = (container: Element | null) => {
    if (!(container instanceof Element)) {
      return;
    }

    execution = attachMountable(container, TestResultExecutionView(options));
  };

  Object.assign(el, {
    render() {
      destroyBlocks();
      destroyExecution();
      el.className = "test-result-overview";
      el.replaceChildren(
        createTestResultOverviewContent({
          cls: "test-result-overview",
          status: options.data.status || "unknown",
          statusMessage:
            typeof options.data.statusMessage === "string" ? options.data.statusMessage : undefined,
          statusTrace:
            typeof options.data.statusTrace === "string" ? options.data.statusTrace : undefined,
        }),
      );
      el.showBlock(el.querySelector(".test-result-overview__tags"), getTestResultBlocks("tag"));
      el.showBlock(
        el.querySelector(".test-result-overview__before"),
        getTestResultBlocks("before"),
      );
      mountExecution(el.querySelector(".test-result-overview__execution"));
      el.showBlock(el.querySelector(".test-result-overview__after"), getTestResultBlocks("after"));
      return el;
    },
    attachToDom() {
      blocks.forEach((block) => block.attachToDom?.());
      execution?.attachToDom?.();
    },
    detachFromDom() {
      execution?.detachFromDom?.();
      blocks.forEach((block) => block.detachFromDom?.());
    },
    destroy() {
      el.detachFromDom?.();
      destroyExecution();
      destroyBlocks();
      el.remove();
    },
  });

  return el;
};

export default createTestResultOverviewView;
