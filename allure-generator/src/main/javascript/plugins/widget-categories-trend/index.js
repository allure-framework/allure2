import CategoriesTrendWidgetView from './CategoriesTrendWidgetView';
import TrendCollection from '../../data/trend/TrendCollection';

allure.api.addWidget(
    'graph',
    'categories-trend',
    CategoriesTrendWidgetView,
    TrendCollection
);