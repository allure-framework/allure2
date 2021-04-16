import TrendCollection from "../../data/trend/TrendCollection";
import HistoryTrendWidgetView from "./HistoryTrendWidgetView";

allure.api.addWidget("widgets", "history-trend", HistoryTrendWidgetView, TrendCollection);

allure.api.addWidget("graph", "history-trend", HistoryTrendWidgetView, TrendCollection);
