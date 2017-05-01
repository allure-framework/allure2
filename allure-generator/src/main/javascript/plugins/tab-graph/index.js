import GraphLayout from './GraphLayout';

allure.api.addTab('graph', {
    title: 'tab.graph.name', icon: 'fa fa-bar-chart',
    route: 'graph',
    onEnter: () => new GraphLayout()
});
