import TimelineLayout from "../tree/layouts/TimelineLayout.mts";

type TabDescriptor = import("../../core/registry/types.mts").TabDescriptor;

export const timelineTab: TabDescriptor = {
  tabName: "timeline",
  title: "tab.timeline.name",
  icon: "lineChartsTimeline",
  route: "timeline",
  onEnter: () => TimelineLayout({ url: "data/timeline.json" }),
};
