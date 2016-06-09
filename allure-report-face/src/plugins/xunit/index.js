import allurePlugins from '../../pluginApi';
import XUnitLayout from './XUnitLayout';
import XUnitWidget from './XUnitWidget';

allurePlugins.addTab('xUnit', {
    title: 'xUnit', icon: 'fa fa-briefcase',
    route: 'xUnit(/:testcaseId)(/:attachmentId)',
    onEnter: (...routeParams) => new XUnitLayout({routeParams})
});
allurePlugins.addWidget('xunit', XUnitWidget);
allurePlugins.addTranslation('en', require('./translations/en.json'));
allurePlugins.addTranslation('ru', require('./translations/ru.json'));
allurePlugins.addTranslation('ptbr', require('./translations/ptbr.json'));