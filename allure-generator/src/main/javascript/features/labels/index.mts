import { createTreeTab } from "../tree/createTreeTab.mts";

export const tagsTab = createTreeTab({
  baseUrl: "tags",
  title: "tab.tags.name",
  icon: "lineHelpersFlag",
  url: "data/tags.json",
});

export const issuesTab = createTreeTab({
  baseUrl: "issues",
  title: "tab.issues.name",
  icon: "lineGeneralLink1",
  url: "data/issues.json",
});

export const testTypesTab = createTreeTab({
  baseUrl: "test-types",
  title: "tab.testTypes.name",
  icon: "lineGeneralChecklist3",
  url: "data/test-types.json",
});
