import allurePlugins from '../../pluginApi';
import XUnitLayout from './XUnitLayout';
import XUnitWidget from './XUnitWidget';

allurePlugins.addTab('xUnit', {
    title: 'xUnit', icon: 'fa fa-briefcase',
    route: 'xUnit(/:defectId)(/:testcaseId)(/:attachmentId)',
    onEnter: (...routeParams) => new XUnitLayout({routeParams})
});
allurePlugins.addWidget('xunit', XUnitWidget);
