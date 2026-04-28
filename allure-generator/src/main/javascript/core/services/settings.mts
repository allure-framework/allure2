import PersistentStateStore from "../state/PersistentStateStore.mts";

type LanguageId = import("../i18n/index.mts").LanguageId;
type Status = import("../../types/report.mts").Status;

type WidgetArrangement = string[][];

export type TreeSorterName = "sorter.order" | "sorter.name" | "sorter.duration" | "sorter.status";

export type TreeSorting = {
  ascending: boolean;
  sorter: TreeSorterName;
};

export type TreeMark = "flaky" | "newFailed" | "newPassed" | "newBroken" | "retriesStatusChange";

type GlobalSettingsAttributes = Record<string, unknown> & {
  sidebarCollapsed: boolean;
  sideBySidePosition: [number, number];
  language: LanguageId;
};

type PluginSettingsOptions<TAttributes extends Record<string, unknown>> = {
  pluginName: string;
  defaults: Partial<TAttributes>;
  [key: string]: unknown;
};

type WidgetGridSettingsAttributes = Record<string, unknown> & {
  widgets: WidgetArrangement;
};

type TreeSettingsAttributes = Record<string, unknown> & {
  visibleStatuses: Record<Status, boolean>;
  visibleMarks: Record<TreeMark, boolean>;
  showGroupInfo: boolean;
  treeSorting: TreeSorting;
};

const globalSettingsDefaults: Pick<
  GlobalSettingsAttributes,
  "sidebarCollapsed" | "sideBySidePosition"
> = {
  sidebarCollapsed: false,
  sideBySidePosition: [50, 50],
};

const treePluginDefaults: TreeSettingsAttributes = {
  visibleStatuses: {
    failed: true,
    broken: true,
    skipped: true,
    unknown: true,
    passed: true,
  },
  visibleMarks: {
    flaky: false,
    newFailed: false,
    newPassed: false,
    newBroken: false,
    retriesStatusChange: false,
  },
  showGroupInfo: false,
  treeSorting: {
    ascending: true,
    sorter: "sorter.name",
  },
};

const widgetGridPluginDefaults: WidgetGridSettingsAttributes = {
  widgets: [[], []],
};

const createSettings = <TStore,>(store: TStore) => store;

class GlobalSettingsStore extends PersistentStateStore<GlobalSettingsAttributes> {
  constructor() {
    super({
      storageKey: "ALLURE_REPORT_SETTINGS",
      defaults: {
        ...globalSettingsDefaults,
        language: (document.documentElement.lang || "en") as LanguageId,
      },
    });
  }

  getLanguage() {
    return (this.get("language") || "en") as LanguageId;
  }

  setLanguage(value: LanguageId) {
    return this.save("language", value);
  }

  isSidebarCollapsed() {
    return this.get("sidebarCollapsed");
  }

  setSidebarCollapsed(value: boolean) {
    return this.save("sidebarCollapsed", value);
  }

  getSideBySidePosition() {
    return this.get("sideBySidePosition");
  }

  setSideBySidePosition(size: [number, number]) {
    return this.save("sideBySidePosition", size);
  }
}

class PluginSettingsStore<
  TAttributes extends Record<string, unknown> = Record<string, unknown>,
> extends PersistentStateStore<TAttributes> {
  constructor(options: PluginSettingsOptions<TAttributes>) {
    super({
      storageKey: `ALLURE_REPORT_SETTINGS_${options.pluginName.toUpperCase()}`,
      defaults: options.defaults,
    });
  }
}

class WidgetGridSettingsStore extends PluginSettingsStore<WidgetGridSettingsAttributes> {
  getWidgetsArrangement() {
    return this.get("widgets");
  }

  setWidgetsArrangement(value: WidgetArrangement) {
    return this.save("widgets", value);
  }
}

class TreeSettingsStore extends PluginSettingsStore<TreeSettingsAttributes> {
  getVisibleStatuses() {
    return this.get("visibleStatuses");
  }

  setVisibleStatuses(value: Record<Status, boolean>) {
    return this.save("visibleStatuses", value);
  }

  getVisibleMarks() {
    return this.get("visibleMarks");
  }

  setVisibleMarks(value: Record<TreeMark, boolean>) {
    return this.save("visibleMarks", value);
  }

  getTreeSorting() {
    return this.get("treeSorting");
  }

  setTreeSorting(value: TreeSorting) {
    return this.save("treeSorting", value);
  }

  isShowGroupInfo() {
    return this.get("showGroupInfo");
  }

  setShowGroupInfo(value: boolean) {
    return this.save("showGroupInfo", value);
  }
}

const getGlobalSettings = () => createSettings(new GlobalSettingsStore());

export const getSettingsForPlugin = <TAttributes extends Record<string, unknown>>(
  pluginName: string,
  defaults: Partial<TAttributes> = {},
) => {
  return createSettings(new PluginSettingsStore({ pluginName, defaults }));
};

export const getSettingsForWidgetGridPlugin = (
  pluginName: string,
  defaults: WidgetGridSettingsAttributes = widgetGridPluginDefaults,
) => {
  return createSettings(new WidgetGridSettingsStore({ pluginName, defaults }));
};

export const getSettingsForTreePlugin = (
  pluginName: string,
  defaults: TreeSettingsAttributes = treePluginDefaults,
) => {
  return createSettings(new TreeSettingsStore({ pluginName, defaults }));
};

const settings = getGlobalSettings();

export default settings;
