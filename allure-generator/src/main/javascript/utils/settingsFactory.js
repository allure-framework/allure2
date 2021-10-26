import LocalStorageModel from "../data/localstorage/LocalStorageModel";

const globalSettingsDefaults = {
  language: "en",
  sidebarCollapsed: false,
  sideBySidePosition: [50, 50],
};

const treePluginDefaults = {
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
    retriesStatusChange : false,
  },
  showGroupInfo: false,
  treeSorting: {
    ascending: true,
    sorter: "sorter.name",
  },
};

const widgetGridPluginDefaults = {
  widgets: [[], []],
};

function getGlobalSettings() {
  const SettingsModel = LocalStorageModel.extend({
    defaults() {
      return globalSettingsDefaults;
    },

    getLanguage() {
      return this.get("language");
    },

    setLanguage(value) {
      return this.save("language", value);
    },

    isSidebarCollapsed() {
      return this.get("sidebarCollapsed");
    },

    setSidebarCollapsed(value) {
      return this.save("sidebarCollapsed", value);
    },

    getSideBySidePosition() {
      return this.get("sideBySidePosition");
    },

    setSideBySidePosition(size) {
      return this.save("sideBySidePosition", size);
    },
  });
  const settings = new SettingsModel();
  settings.fetch();
  return settings;
}

function getSettingsForPlugin(pluginName, defaults = {}) {
  const SettingsModel = LocalStorageModel.extend({
    storageKey() {
      return `ALLURE_REPORT_SETTINGS_${pluginName.toUpperCase()}`;
    },
    defaults() {
      return defaults;
    },
  });
  const settings = new SettingsModel();
  settings.fetch();
  return settings;
}

function getSettingsForWidgetGridPlugin(pluginName, defaults = widgetGridPluginDefaults) {
  const SettingsModel = LocalStorageModel.extend({
    storageKey() {
      return `ALLURE_REPORT_SETTINGS_${pluginName.toUpperCase()}`;
    },
    defaults() {
      return defaults;
    },
    getWidgetsArrangement() {
      return this.get("widgets");
    },
    setWidgetsArrangement(value) {
      this.save("widgets", value);
    },
  });
  const settings = new SettingsModel();
  settings.fetch();
  return settings;
}

function getSettingsForTreePlugin(pluginName, defaults = treePluginDefaults) {
  const SettingsModel = LocalStorageModel.extend({
    storageKey() {
      return `ALLURE_REPORT_SETTINGS_${pluginName.toUpperCase()}`;
    },
    defaults() {
      return defaults;
    },

    getVisibleStatuses() {
      return this.get("visibleStatuses");
    },

    setVisibleStatuses(value) {
      return this.save("visibleStatuses", value);
    },

    getVisibleMarks() {
      return this.get("visibleMarks");
    },

    setVisibleMarks(value) {
      return this.save("visibleMarks", value);
    },

    getTreeSorting() {
      return this.get("treeSorting");
    },

    setTreeSorting(value) {
      this.save("treeSorting", value);
    },

    isShowGroupInfo() {
      return this.get("showGroupInfo");
    },

    setShowGroupInfo(value) {
      this.save("showGroupInfo", value);
    },
  });
  const settings = new SettingsModel();
  settings.fetch();
  return settings;
}

export {
  getGlobalSettings,
  getSettingsForPlugin,
  getSettingsForTreePlugin,
  getSettingsForWidgetGridPlugin
};
