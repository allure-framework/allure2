import DefectsLayout from './DefectsLayout';
import DefectsWidget from './defects-widget/DefectsWidget';

allure.api.addTab('defects', {
    title: 'Defects', icon: 'fa fa-flag',
    route: 'defects(/:defectId)(/:testcaseId)',
    onEnter: (...routeParams) => new DefectsLayout({routeParams})
});
allure.api.addWidget('productDefects', DefectsWidget);
allure.api.addWidget('testDefects', DefectsWidget);
allure.api.addTranslation('en', require('./translations/en.json'));
allure.api.addTranslation('ru', require('./translations/ru.json'));
allure.api.addTranslation('ptbr', require('./translations/ptbr.json'));
