import { createAllureIconElement } from "../../../helpers/allure-icon.mts";
import dateHelper from "../../../helpers/date.mts";
import duration from "../../../helpers/duration.mts";
import fileicon from "../../../helpers/fileicon.mts";
import filesize from "../../../helpers/filesize.mts";
import translate from "../../../helpers/t.mts";
import { createTextWithLinksFragment } from "../../../helpers/text-with-links.mts";
import timeHelper from "../../../helpers/time.mts";
import b from "../../../shared/bem/index.mts";
import { createElement, createFragment } from "../../../shared/dom.mts";
import { createIconElement } from "../../../shared/icon/index.mts";
import attachmentType from "../../attachments/model/attachmentType.mts";
import { createStatusDetailsElement } from "./renderStatusDetails.mts";
type Attachment = import("../../../types/report.mts").Attachment;
type Parameter = import("../../../types/report.mts").Parameter;
type Stage = import("../../../types/report.mts").Stage;
type Step = import("../../../types/report.mts").Step;

type ExecutionTemplateOptions = {
  hasContent: boolean;
  before: Stage[];
  test: Stage[];
  after: Stage[];
};

const createParametersTable = ({ parameters }: { parameters?: Parameter[] }) =>
  parameters?.length
    ? createElement("div", {
        className: "parameters-table",
        children: parameters.map(({ name, value }) =>
          createElement("div", {
            className: b("parameters-table", "row"),
            children: [
              createElement("div", {
                className: `${b("parameters-table", "cell", { name: true })} line-ellipsis`,
                text: name,
              }),
              createElement("div", {
                className: `${b("parameters-table", "cell", { value: true })} line-ellipsis`,
                text: value,
              }),
            ],
          }),
        ),
      })
    : null;

const createStepStats = ({
  parameters,
  stepsCount,
  attachmentsCount,
  time,
}: {
  parameters?: Parameter[];
  stepsCount?: number;
  attachmentsCount?: number;
  time?: import("../../../types/report.mts").Time;
}) => {
  const stats: string[] = [];

  if (parameters?.length) {
    stats.push(
      translate("testResult.stats.count.parameters", { hash: { count: parameters.length } }),
    );
  }
  if (stepsCount) {
    stats.push(translate("testResult.stats.count.steps", { hash: { count: stepsCount } }));
  }
  if (attachmentsCount) {
    stats.push(
      translate("testResult.stats.count.attachments", { hash: { count: attachmentsCount } }),
    );
  }

  return {
    summary: stats.length
      ? createElement("span", {
          className: "step-stats__summary",
          children: stats.flatMap((stat, index) => (index === 0 ? [stat] : [",\u00a0", stat])),
        })
      : null,
    time: createElement("span", {
      attrs: {
        "data-tooltip": `${dateHelper(time?.start)} ${timeHelper(time?.start, true)} \u2013 ${timeHelper(time?.stop, true)}`,
      },
      className: "step-stats__time",
      text: duration(time?.duration),
    }),
  };
};

const createAttachmentRow = ({ uid, type, name, source, size }: Attachment) =>
  (() => {
    const attachmentInfo = attachmentType(type || "");
    const isTraceAttachment = attachmentInfo.type === "playwright-trace";

    return createElement("div", {
      children: [
        createElement("div", {
          attrs: {
            "data-type": type,
            "data-uid": uid,
            ...(isTraceAttachment ? { "data-viewer": "playwright-trace" } : {}),
          },
          className: "attachment-row",
          children: [
            createElement("span", {
              className: "attachment-row__arrow block__arrow",
              children: createIconElement(
                isTraceAttachment ? "lineGeneralLinkExternal" : "lineArrowsChevronRight",
                {
                  className: "angle",
                  size: "s",
                },
              ),
            }),
            createElement("div", {
              attrs: type ? { "data-tooltip": type } : {},
              className: "attachment-row__icon",
              children: createIconElement(fileicon(type), {
                size: "s",
                title: type || "",
              }),
            }),
            createElement("div", {
              className: "attachment-row__name long-line",
              text: name || source,
            }),
            createElement("div", {
              className: "attachment-row__control attachment-row__link",
              children: createElement("div", {
                attrs: {
                  "data-download": `data/attachments/${source}`,
                  "data-download-target": "_blank",
                  "data-download-type": type,
                  "data-tooltip": translate("testResult.execution.downloadAttachment.tooltip"),
                },
                className: "link",
                children: [
                  createIconElement("lineGeneralDownloadCloud", {
                    inline: true,
                    size: "s",
                  }),
                  " ",
                  filesize(size),
                ],
              }),
            }),
            createElement("div", {
              className: "attachment-row__control attachment-row__fullscreen",
              children: createElement("a", {
                className: "link",
                children: createIconElement("lineLayoutsMaximize2", {
                  inline: true,
                  size: "s",
                }),
              }),
            }),
          ],
        }),
        createElement("div", {
          className: "attachment-row__preview",
          children: createElement("div", {
            className: `attachment-row__content ${b("attachment", String(uid))}`,
          }),
        }),
      ],
    });
  })();

const createStepTitle = ({
  hasContent,
  isStage = false,
  name,
  status,
  linkifiedName = false,
  statsSummary,
  statsTime,
}: {
  hasContent: boolean;
  isStage?: boolean;
  name: string;
  status?: string;
  linkifiedName?: boolean;
  statsSummary?: Node | null;
  statsTime?: Node | null;
}) =>
  createElement("div", {
    className: b("step", "title", { hasContent, stage: isStage }),
    children: [
      hasContent
        ? createElement("span", {
            className: "step__arrow block__arrow",
            children: createIconElement("lineArrowsChevronRight", {
              className: `text_status_${status || "unknown"}`,
              size: "s",
            }),
          })
        : createElement("span", {
            className: "step__status",
            children: createAllureIconElement(status || "unknown"),
          }),
      createElement("div", {
        className: "step__main long-line",
        children: [
          createElement("div", {
            className: "step__name",
            children: linkifiedName ? createTextWithLinksFragment(name) : name,
          }),
          statsSummary,
        ],
      }),
      statsTime,
    ],
  });

const createStepContent = (step: Step): DocumentFragment | HTMLElement => {
  if (step.attachmentStep) {
    return createElement("div", {
      className: "step",
      children: (step.attachments || []).map((attachment) => createAttachmentRow(attachment)),
    });
  }

  return createElement("div", {
    className: "step",
    children: [
      (() => {
        const stats = createStepStats(step);
        return createStepTitle({
          hasContent: Boolean(step.hasContent),
          linkifiedName: true,
          name: step.name || "",
          status: step.status || "unknown",
          statsSummary: stats.summary,
          statsTime: stats.time,
        });
      })(),
      createElement("div", {
        className: "step__content",
        children: [
          createParametersTable({ parameters: step.parameters }),
          createStepsList(step.steps),
          ...(step.attachments || []).map((attachment) => createAttachmentRow(attachment)),
          step.shouldDisplayMessage ? createStatusDetailsElement(step) : null,
        ],
      }),
    ],
  });
};

const createStepsList = (steps: Step[] | undefined) =>
  createFragment(...(steps || []).map((step) => createStepContent(step)));

const createStageBody = (stage: Stage) =>
  stage.name
    ? createElement("div", {
        className: b("step"),
        children: [
          (() => {
            const stats = createStepStats(stage);
            return createStepTitle({
              hasContent: Boolean(stage.hasContent),
              isStage: true,
              name: stage.name,
              status: stage.status || "unknown",
              statsSummary: stats.summary,
              statsTime: stats.time,
            });
          })(),
          createElement("div", {
            className: b("step", "content"),
            children: [
              createParametersTable({ parameters: stage.parameters }),
              createStepsList(stage.steps),
              ...(stage.attachments || []).map((attachment) => createAttachmentRow(attachment)),
              stage.shouldDisplayMessage ? createStatusDetailsElement(stage) : null,
            ],
          }),
        ],
      })
    : createFragment(
        createStepsList(stage.steps),
        ...(stage.attachments || []).map((attachment) => createAttachmentRow(attachment)),
        stage.shouldDisplayMessage ? createStatusDetailsElement(stage) : null,
      );

const createStagesBlock = ({
  stages,
  name,
  expanded,
}: {
  stages?: Stage[];
  name: string;
  expanded: boolean;
}) =>
  stages?.length
    ? createElement("div", {
        className: b("step", { expanded }),
        children: [
          createElement("div", {
            className: b("step", "title", { hasContent: true, stage: true }),
            children: [
              createElement("span", {
                className: "step__arrow block__arrow",
                children: createIconElement("lineArrowsChevronRight", {
                  className: "angle",
                  size: "s",
                }),
              }),
              name,
            ],
          }),
          createElement("div", {
            className: b("step", "content"),
            children: stages.map((stage) => createStageBody(stage)),
          }),
        ],
      })
    : null;

export const createTestResultExecutionContent = ({
  hasContent,
  before,
  test,
  after,
}: ExecutionTemplateOptions) =>
  createFragment(
    createElement("h3", {
      className: "test-result-execution__title",
      text: translate("testResult.execution.name"),
    }),
    hasContent
      ? createElement("div", {
          className: "execution__content",
          children: [
            createStagesBlock({
              stages: before,
              name: translate("testResult.execution.setup"),
              expanded: false,
            }),
            createStagesBlock({
              stages: test,
              name: translate("testResult.execution.body"),
              expanded: true,
            }),
            createStagesBlock({
              stages: after,
              name: translate("testResult.execution.teardown"),
              expanded: false,
            }),
          ],
        })
      : createElement("div", {
          className: b("pane", "section"),
          text: translate("testResult.execution.empty"),
        }),
  );
