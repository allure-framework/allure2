import {
  attachmentTestResultBlocks,
  attachmentViewers,
} from "../../features/attachments/index.mts";
import { categoriesTab } from "../../features/categories/index.mts";
import { behaviorsTab } from "../../features/behaviors/index.mts";
import {
  graphTab,
  overviewTab,
  widgetsByTab as overviewWidgets,
} from "../../features/overview/index.mts";
import { packagesTab } from "../../features/packages/index.mts";
import { suitesTab } from "../../features/suites/index.mts";
import { testResultBlocks, testResultTabs } from "../../features/test-result/index.mts";
import { timelineTab } from "../../features/timeline/index.mts";

type AttachmentViewers = import("./types.mts").AttachmentViewers;
type AvailabilityResolver = import("./types.mts").AvailabilityResolver;
type Mountable = import("./types.mts").Mountable;
type RouteArguments = import("./types.mts").RouteArguments;
type RuntimeWidgetDescriptor = import("./types.mts").RuntimeWidgetDescriptor;
type TabDescriptor = import("./types.mts").TabDescriptor;
type TestResultBlockPosition = import("./types.mts").TestResultBlockPosition;
type TestResultBlocks = import("./types.mts").TestResultBlocks;
type WidgetDescriptor = import("./types.mts").WidgetDescriptor;
type WidgetsByTab = import("./types.mts").WidgetsByTab;
type AppRouter = Pick<typeof import("../routing/router.mts").default, "route">;

type RegisterTabRoutesOptions = {
  notFound: () => Mountable;
  router: AppRouter;
  showView: <TArgs extends RouteArguments>(
    factory: (...args: TArgs) => Mountable,
  ) => (...args: TArgs) => void;
};

const defaultAvailability: AvailabilityResolver = () => true;

const tabs: TabDescriptor[] = [
  overviewTab,
  categoriesTab,
  suitesTab,
  behaviorsTab,
  packagesTab,
  graphTab,
  timelineTab,
];

const widgets: WidgetsByTab = { ...overviewWidgets };

const blocksByPosition: TestResultBlocks = {
  tag: [...testResultBlocks.tag, ...attachmentTestResultBlocks.tag],
  before: [...testResultBlocks.before, ...attachmentTestResultBlocks.before],
  after: [...testResultBlocks.after, ...attachmentTestResultBlocks.after],
};

export const getTabs = () => tabs.filter(({ available = defaultAvailability }) => available());

export const getWidgets = (tabName: string) =>
  (widgets[tabName] || []).reduce<Record<string, RuntimeWidgetDescriptor>>((result, descriptor) => {
    if ((descriptor.available || defaultAvailability)()) {
      result[descriptor.widgetName] = {
        available: descriptor.available || defaultAvailability,
        load: descriptor.load,
        create: descriptor.create,
      };
    }

    return result;
  }, {});

export const getTestResultBlocks = (position: TestResultBlockPosition) =>
  blocksByPosition[position] || [];

export const getTestResultTabs = () => [...testResultTabs];

export const getAttachmentViewer = (mimeType: string) =>
  (attachmentViewers as AttachmentViewers)[mimeType] || null;

export const registerTabRoutes = ({ notFound, router, showView }: RegisterTabRoutesOptions) => {
  tabs.forEach(({ tabName, route, onEnter = notFound }) => {
    router.route(
      route,
      tabName,
      showView((...args) => onEnter(...args)),
    );
  });
};

export type { WidgetDescriptor };
