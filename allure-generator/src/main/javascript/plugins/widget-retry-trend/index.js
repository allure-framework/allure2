import TrendCollection from "../../data/trend/TrendCollection";
import RetryTrendWidgetView from "./RetryTrendWidgetView";

allure.api.addWidget("graph", "retry-trend", RetryTrendWidgetView, TrendCollection);
