import GraphLayout from './GraphLayout';
import GraphWidget from './graph-widget/GraphWidget';

allure.api.addTab('graph', {
    title: 'Graph', icon: 'fa fa-bar-chart',
    route: 'graph',
    onEnter: () => new GraphLayout()
});
allure.api.addWidget('total', GraphWidget);
