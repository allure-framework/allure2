import TreeLayout from "./layouts/TreeLayout.mts";

type AvailabilityResolver = import("../../core/registry/types.mts").AvailabilityResolver;
type IconName = import("../../shared/icon/index.mts").IconName;
type RouteParameter = import("../../core/registry/types.mts").RouteParameter;
type TabDescriptor = import("../../core/registry/types.mts").TabDescriptor;
type TranslationKey = import("../../core/i18n/types.mts").TranslationKey;

export type TreeTabOptions = {
  baseUrl: string;
  csvUrl?: string;
  icon: IconName;
  title: TranslationKey;
  url: string;
  available?: AvailabilityResolver;
};

export const createTreeTab = ({
  baseUrl,
  csvUrl,
  icon,
  title,
  url,
  available = () => true,
}: TreeTabOptions): TabDescriptor => ({
  available,
  tabName: baseUrl,
  title,
  icon,
  route: `${baseUrl}(/)(:testGroup)(/)(:testResult)(/)(:testResultTab)(/)`,
  onEnter: (
    testGroup: RouteParameter = null,
    testResult: RouteParameter = null,
    testResultTab: RouteParameter = null,
  ) =>
    TreeLayout({
      testGroup: testGroup || undefined,
      testResult: testResult || undefined,
      testResultTab: testResultTab || undefined,
      tabName: title,
      baseUrl,
      url,
      csvUrl,
    }),
});
