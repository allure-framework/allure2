import allurePlugins from '../../pluginApi';
import GraphLayout from './GraphLayout';

allurePlugins.addTab('graph', {
    title: 'Graph', icon: 'fa fa-bar-chart',
    route: 'graph',
    onEnter: () => new GraphLayout()
});
