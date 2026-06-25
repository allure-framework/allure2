import { ScreenDiffTestResultView } from "./views/ScreenDiffView.mts";

type TestResultBlockFactory = import("../../core/registry/types.mts").TestResultBlockFactory;
type TestResultBlocks = import("../../core/registry/types.mts").TestResultBlocks;

export const attachmentTestResultBlocks: TestResultBlocks = {
  tag: [],
  before: [ScreenDiffTestResultView] as TestResultBlockFactory[],
  after: [],
};
