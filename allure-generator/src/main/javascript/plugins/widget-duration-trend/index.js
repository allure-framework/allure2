import DurationTrendWidgetView from './DurationTrendWidgetView';
import TrendCollection from '../../data/trend/TrendCollection';

allure.api.addWidget(
    'graph',
    'duration-trend',
    DurationTrendWidgetView,
    TrendCollection
);