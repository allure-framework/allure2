import WidgetsGridView from "../../components/widgets-grid/WidgetsGridView";
import AppLayout from "../application/AppLayout";

export default class OverviewLayout extends AppLayout {
  getContentView() {
    return new WidgetsGridView({ tabName: "widgets" });
  }
}
