import allurePlugins from '../../pluginApi';
import GraphLayout from './GraphLayout';
import GraphWidget from './graph-widget/GraphWidget';

allurePlugins.addTab('graph', {
    title: 'Graph', icon: 'fa fa-bar-chart',
    route: 'graph',
    onEnter: () => new GraphLayout()
});
allurePlugins.addWidget('total', GraphWidget);
