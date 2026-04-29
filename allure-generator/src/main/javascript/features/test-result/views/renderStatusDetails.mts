import ansi from "../../../helpers/ansi.mts";
import translate from "../../../helpers/t.mts";
import b from "../../../shared/bem/index.mts";
import { createElement, createFragmentFromHtml } from "../../../shared/dom.mts";

type StatusDetailsRenderData = {
  status?: string;
  statusMessage?: string;
  statusTrace?: string;
};

const createCodeBlock = ({ className, content }: { className: string; content: string }) => {
  const pre = createElement("pre", { className });
  const code = createElement("code");
  code.append(createFragmentFromHtml(content, pre));
  pre.append(code);
  return pre;
};

export const createStatusDetailsElement = ({
  status,
  statusMessage,
  statusTrace,
}: StatusDetailsRenderData) => {
  const safeStatus = status || "unknown";
  const className = b("status-details", { status: safeStatus });
  const root = createElement("div", { className });
  const content = createElement("div", { className: "status-details__content" });

  if (statusMessage || statusTrace) {
    const emptyText = translate("testResult.status.empty");
    const traceToggle = createElement("div", {
      attrs: {
        "data-ga4-event": "stacktrace_expand_click",
        "data-tooltip": translate("testResult.status.trace"),
      },
      className: `${b("status-details", "trace-toggle", { status: safeStatus })} clickable`,
      children: createCodeBlock({
        className: "status-details__message",
        content: statusMessage ? ansi(statusMessage) : emptyText,
      }),
    });

    const trace = createCodeBlock({
      className: b("status-details", "trace"),
      content: statusTrace ? ansi(statusTrace) : emptyText,
    });

    content.append(traceToggle, trace);
  }

  root.append(content);
  return root;
};
