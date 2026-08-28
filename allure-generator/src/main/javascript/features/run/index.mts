import RunLayout from "./RunLayout.mts";

type TabDescriptor = import("../../core/registry/types.mts").TabDescriptor;

export const runTab: TabDescriptor = {
  tabName: "run",
  title: "tab.run.name",
  icon: "lineGeneralInfoCircle",
  route: "run",
  onEnter: () => RunLayout(),
};
