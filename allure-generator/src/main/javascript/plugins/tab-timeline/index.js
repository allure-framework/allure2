import TimelineLayout from './TimelineLayout';

allure.api.addTab('timeline', {
    title: 'Timeline',
    icon: 'fa fa-clock-o',
    route: 'timeline',
    onEnter: (...routeParams) => new TimelineLayout({routeParams})
});
