import AppLayout from "../../shell/AppLayout.mts";
import { loadTreeData, type LoadedTreeData } from "../model/treeData.mts";
import TimelineView from "../views/TimelineView.mts";

export default function TimelineLayout(options: { url: string } & Record<string, unknown>) {
  let treeData: LoadedTreeData | null = null;

  return AppLayout({
    ...options,
    contentScrollable: false,
    loadData: async () => {
      treeData = await loadTreeData(options.url);
    },
    createContentView: () => {
      if (!treeData) {
        throw new Error(`Tree data "${options.url}" is not loaded`);
      }

      return new TimelineView({ treeData });
    },
  });
}
