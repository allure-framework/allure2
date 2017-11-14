import RetryTrendWidgetView from './RetryTrendWidgetView';
import TrendCollection from '../../data/trend/TrendCollection';

allure.api.addWidget(
    'graph',
    'retry-trend',
    RetryTrendWidgetView,
    TrendCollection
);