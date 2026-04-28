export type Status = "failed" | "broken" | "passed" | "skipped" | "unknown";

export type Statistic = Partial<Record<Status, number>> & {
  total?: number;
};

export type Time = {
  start?: number;
  stop?: number;
  duration?: number;
  minDuration?: number;
  maxDuration?: number;
  sumDuration?: number;
};

export type Label = {
  name: string;
  value: string;
};

export type Parameter = {
  name?: string;
  value?: string;
};

export type Attachment = {
  name?: string;
  source: string;
  type: string;
  uid?: string | number;
  size?: number;
};

export type Step = {
  name?: string;
  description?: string;
  status?: Status;
  time?: Time;
  steps?: Step[];
  attachments?: Attachment[];
  parameters?: Parameter[];
  shouldDisplayMessage?: boolean;
  stepsCount?: number;
  attachmentsCount?: number;
  hasContent?: boolean;
  attachmentStep?: boolean;
};

export type Stage = Step;

export type HistoryItem = {
  uid: string;
  status: Status;
  time?: Time;
  statusDetails?: string;
};

export type TestResultExtra = {
  severity?: string;
  owner?: string;
  retries?: HistoryItem[];
  categories?: unknown[];
  history?: {
    statistic?: Statistic;
    items?: HistoryItem[];
  };
  tags?: string[];
  [key: string]: unknown;
};

export type TestResult = {
  uid: string;
  name?: string;
  fullName?: string;
  historyId?: string;
  time?: Time;
  description?: string;
  descriptionHtml?: string;
  status?: Status;
  flaky?: boolean;
  newFailed?: boolean;
  newBroken?: boolean;
  newPassed?: boolean;
  retriesCount?: number;
  retriesStatusChange?: boolean;
  beforeStages?: Stage[];
  testStage?: Stage;
  afterStages?: Stage[];
  labels?: Label[];
  parameters?: Parameter[];
  links?: unknown[];
  hidden?: boolean;
  retry?: boolean;
  extra?: TestResultExtra;
  source?: string;
  parameterValues?: string[];
  [key: string]: unknown;
};

export type TreeNode = {
  name?: string;
  uid?: string;
  parentUid?: string;
  status?: Status;
  time?: Time;
  flaky?: boolean;
  newFailed?: boolean;
  newPassed?: boolean;
  newBroken?: boolean;
  retriesCount?: number;
  retriesStatusChange?: boolean;
  parameters?: string[];
  tags?: string[];
  children?: TreeNode[];
  statistic?: Statistic;
  order?: number;
  [key: string]: unknown;
};

export type TreeRoot = {
  uid: string;
  name?: string;
  children?: TreeNode[];
};

export type TrendData = Record<string, number> & {
  total?: number;
};

export type RawTrendPoint = {
  buildOrder?: string | number;
  data: TrendData;
  [key: string]: unknown;
};

export type TrendPoint = Omit<RawTrendPoint, "data"> & {
  id: number;
  name: string;
  total: number;
  data: Record<string, number>;
};

export type WidgetAttributes = Record<string, unknown> & {
  items?: unknown[];
  statistic?: Statistic;
};

export type GraphResultItem = {
  uid: string;
  name?: string;
  time?: Time;
  status: Status;
  severity?: string;
};

export type ScreenDiffPayload = {
  diff: string;
  actual: string;
  expected: string;
};
