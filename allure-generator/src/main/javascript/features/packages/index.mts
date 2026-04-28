import { createTreeTab } from "../tree/createTreeTab.mts";

export const packagesTab = createTreeTab({
  baseUrl: "packages",
  title: "tab.packages.name",
  icon: "lineDevCodeSquare",
  url: "data/packages.json",
});
