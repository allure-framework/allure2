import { createTreeTab } from "../tree/createTreeTab.mts";

export const tagsTab = createTreeTab({
  baseUrl: "tags",
  title: "testResult.tags.name",
  icon: "lineGeneralChecklist3",
  url: "data/tags.json",
});
