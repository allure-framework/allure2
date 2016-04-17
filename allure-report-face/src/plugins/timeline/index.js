import allurePlugins from '../../pluginApi';
import TimelineLayout from './TimelineLayout';

allurePlugins.addTab('timeline', {
    title: 'Timeline',
    icon: 'fa fa-clock-o',
    route: 'timeline(/:testcaseId)(/:attachmentId)',
    onEnter: (...routeParams) => new TimelineLayout({routeParams})
});

