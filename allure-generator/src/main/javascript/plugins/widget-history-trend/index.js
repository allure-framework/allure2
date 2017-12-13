import HistoryTrendWidgetView from './HistoryTrendWidgetView';
import TrendCollection from '../../data/trend/TrendCollection';

allure.api.addWidget(
    'widgets',
    'history-trend',
    HistoryTrendWidgetView,
    TrendCollection
);

allure.api.addWidget(
    'graph',
    'history-trend',
    HistoryTrendWidgetView,
    TrendCollection
);