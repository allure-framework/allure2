import allurePlugins from '../../pluginApi';
import DefectsLayout from './DefectsLayout';
import DefectsWidget from './defects-widget/DefectsWidget';

allurePlugins.addTab('defects', {
    title: 'Defects', icon: 'fa fa-flag',
    route: 'defects(/:defectId)(/:testcaseId)(/:attachmentId)',
    onEnter: (...routeParams) => new DefectsLayout({routeParams})
});
allurePlugins.addWidget('defects', DefectsWidget);
allurePlugins.addTranslation('en', require('./translations/en.json'));
allurePlugins.addTranslation('ru', require('./translations/ru.json'));
allurePlugins.addTranslation('ptbr', require('./translations/ptbr.json'));
