import allurePlugins from '../../pluginApi';
import BehaviorsLayout from './BehaviorsLayout';
import BehaviorsWidget from './BehaviorsWidget';

allurePlugins.addTab('behaviors', {
    title: 'Behaviors', icon: 'fa fa-list',
    route: 'behaviors(/:defectId)(/:testcaseId)(/:attachmentId)',
    onEnter: (...routeParams) => new BehaviorsLayout({routeParams})
});
allurePlugins.addWidget('behaviors', BehaviorsWidget);
