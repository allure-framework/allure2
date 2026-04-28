import AppLayout from "../shell/AppLayout.mts";
import WidgetsGridView from "./WidgetsGridView.mts";

export default function OverviewLayout(options: Record<string, unknown>) {
  return AppLayout({
    ...options,
    createContentView: () => WidgetsGridView({ tabName: "widgets" }),
  });
}
