import allurePlugins from '../../pluginApi';

import OverviewWidget from '../../components/widget-overview/OverviewWidget';
import OverviewLayout from '../../layouts/overview/OverivewLayout';

allurePlugins.addWidget('total', OverviewWidget);

allurePlugins.addTab('', {
    title: 'Overview', icon: 'fa fa-home',
    route: '',
    onEnter: () => new OverviewLayout()
});
