import AppLayout from "../shell/AppLayout.mts";
import { loadGlobalsData } from "./model/globalsData.mts";
import RunView from "./views/RunView.mts";

export default function RunLayout(options: Record<string, unknown> = {}) {
  let data: import("../../types/report.mts").GlobalsData = {
    errors: [],
    attachments: [],
  };

  return AppLayout({
    ...options,
    loadData: async () => {
      data = await loadGlobalsData();
    },
    createContentView: () => RunView({ data }),
  });
}
