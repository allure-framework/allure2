import TrendCollection from "../../data/trend/TrendCollection";
import CategoriesTrendWidgetView from "./CategoriesTrendWidgetView";

allure.api.addWidget("graph", "categories-trend", CategoriesTrendWidgetView, TrendCollection);
