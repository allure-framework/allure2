import AppLayout from "../shell/AppLayout.mts";
import { fetchReportJson } from "../../core/services/reportData.mts";
import { loadGlobalsData } from "../run/model/globalsData.mts";
import RunStatusBannerView from "../run/views/RunStatusBannerView.mts";
import WidgetsGridView from "./WidgetsGridView.mts";

export default function OverviewLayout(options: Record<string, unknown>) {
  let globals: import("../../types/report.mts").GlobalsData = {
    errors: [],
    attachments: [],
  };
  let summary: import("../../types/report.mts").SummaryData = {};

  return AppLayout({
    ...options,
    loadData: async () => {
      [globals, summary] = await Promise.all([
        loadGlobalsData(),
        fetchReportJson<import("../../types/report.mts").SummaryData>("widgets/summary.json"),
      ]);
    },
    createContentView: () =>
      WidgetsGridView({
        tabName: "widgets",
        header: globals.errors.length ? RunStatusBannerView({ globals, summary }) : undefined,
      }),
  });
}
