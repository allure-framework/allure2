import { HTTP_EXCHANGE_ATTACHMENT_MIME_TYPES } from "./model/httpAttachment.mts";
import { HttpAttachmentView } from "./views/HttpAttachmentView.mts";
import { ScreenDiffAttachmentView, ScreenDiffTestResultView } from "./views/ScreenDiffView.mts";

type AttachmentViewers = import("../../core/registry/types.mts").AttachmentViewers;
type TestResultBlockFactory = import("../../core/registry/types.mts").TestResultBlockFactory;
type TestResultBlocks = import("../../core/registry/types.mts").TestResultBlocks;

export const attachmentViewers: AttachmentViewers = {
  ...Object.fromEntries(
    HTTP_EXCHANGE_ATTACHMENT_MIME_TYPES.map((type) => [
      type,
      {
        create: HttpAttachmentView,
        icon: "lineDevDataflow3",
      },
    ]),
  ),
  "application/vnd.allure.image.diff": {
    create: ScreenDiffAttachmentView,
    icon: "lineLayoutsColumns2",
  },
};

export const attachmentTestResultBlocks: TestResultBlocks = {
  tag: [],
  before: [ScreenDiffTestResultView] as TestResultBlockFactory[],
  after: [],
};
