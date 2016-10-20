import GraphLayout from './GraphLayout';

allure.api.addTab('graph', {
    title: 'Graph', icon: 'fa fa-bar-chart',
    route: 'graph',
    onEnter: () => new GraphLayout()
});
