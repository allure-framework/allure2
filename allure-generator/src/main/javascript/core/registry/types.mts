export type Mountable = import("../view/types.mts").Mountable;
export type MountableFactory<
  TOptions = void,
  TMountable extends Mountable = Mountable,
> = import("../view/types.mts").MountableFactory<TOptions, TMountable>;
type IconName = import("../../shared/icon/index.mts").IconName;
type TranslationKey = import("../i18n/types.mts").TranslationKey;
type Attachment = import("../../types/report.mts").Attachment;
type TestResult = import("../../types/report.mts").TestResult;
type TrendPoint = import("../../types/report.mts").TrendPoint;
type WidgetAttributes = import("../../types/report.mts").WidgetAttributes;
type StateStore = import("../state/StateStore.mts").default;
type TestResultAttachmentLookup =
  import("../../features/test-result/model/testResultData.mts").TestResultAttachmentLookup;

export type WidgetLoader<TData = unknown> = () => Promise<TData>;

export type AvailabilityResolver = () => boolean;

export type RouteParameter = string | null;

export type RouteArguments = RouteParameter[];

export type WidgetData = WidgetAttributes | TrendPoint[];

export type WidgetFactory = MountableFactory<{ data?: WidgetData }>;

export type TabDescriptor<TRouteArgs extends RouteArguments = RouteArguments> = {
  tabName: string;
  title: TranslationKey;
  icon: IconName;
  route: string;
  available?: AvailabilityResolver;
  onEnter?: (...args: TRouteArgs) => Mountable;
};

export type WidgetDescriptor = {
  widgetName: string;
  create: WidgetFactory;
  load: WidgetLoader<WidgetData>;
  available?: AvailabilityResolver;
};

export type RuntimeWidgetDescriptor = Pick<WidgetDescriptor, "load" | "create"> & {
  available: AvailabilityResolver;
};

export type WidgetsByTab = Record<string, WidgetDescriptor[]>;

export type TestResultBlockPosition = "tag" | "before" | "after";

export type TestResultBlockOptions = {
  data: TestResult;
};

export type TestResultBlockFactory = MountableFactory<TestResultBlockOptions>;

export type TestResultBlocks = Record<TestResultBlockPosition, TestResultBlockFactory[]>;

export type TestResultTabOptions = {
  data: TestResult;
  attachmentsByUid: TestResultAttachmentLookup;
  routeState: StateStore;
  baseUrl: string;
};

export type TestResultTabFactory = MountableFactory<TestResultTabOptions>;

export type TestResultTabDescriptor = {
  id: string;
  name: TranslationKey;
  create: TestResultTabFactory;
};

export type AttachmentViewerOptions = {
  sourceUrl: string;
  attachment: Attachment;
};

export type AttachmentViewerFactory = MountableFactory<AttachmentViewerOptions>;

export type AttachmentViewerDescriptor = {
  create: AttachmentViewerFactory;
  icon: IconName;
};

export type AttachmentViewers = Record<string, AttachmentViewerDescriptor>;

export const defineWidgetDescriptor = <TData,>(descriptor: {
  widgetName: string;
  create: MountableFactory<{ data?: TData }>;
  load: WidgetLoader<TData>;
  available?: AvailabilityResolver;
}): WidgetDescriptor => descriptor as unknown as WidgetDescriptor;
