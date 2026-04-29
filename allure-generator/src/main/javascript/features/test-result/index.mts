import CategoryView from "./blocks/CategoryView.mts";
import DescriptionView from "./blocks/DescriptionView.mts";
import DurationView from "./blocks/DurationView.mts";
import LinksView from "./blocks/LinksView.mts";
import OwnerView from "./blocks/OwnerView.mts";
import ParametersView from "./blocks/ParametersView.mts";
import SeverityView from "./blocks/SeverityView.mts";
import TagsView from "./blocks/TagsView.mts";
import HistoryView from "./tabs/HistoryView.mts";
import RetriesView from "./tabs/RetriesView.mts";

type TestResultBlockFactory = import("../../core/registry/types.mts").TestResultBlockFactory;
type TestResultBlocks = import("../../core/registry/types.mts").TestResultBlocks;
type TestResultTabDescriptor = import("../../core/registry/types.mts").TestResultTabDescriptor;

export const testResultBlocks: TestResultBlocks = {
  tag: [TagsView, CategoryView, SeverityView, DurationView] as TestResultBlockFactory[],
  before: [DescriptionView, OwnerView, ParametersView, LinksView] as TestResultBlockFactory[],
  after: [],
};

export const testResultTabs: TestResultTabDescriptor[] = [
  { id: "history", name: "testResult.history.name", create: HistoryView },
  { id: "retries", name: "testResult.retries.name", create: RetriesView },
];
