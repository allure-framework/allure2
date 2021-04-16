import TrendCollection from "../../data/trend/TrendCollection";
import DurationTrendWidgetView from "./DurationTrendWidgetView";

allure.api.addWidget("graph", "duration-trend", DurationTrendWidgetView, TrendCollection);
