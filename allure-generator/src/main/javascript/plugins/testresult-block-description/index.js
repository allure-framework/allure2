import TestResultBlockDescriptionView from './TestResultBlockDescriptionView';

allure.api.addTestResultBlock(TestResultBlockDescriptionView, {
    order: 25,
    condition: (model) => model.get('descriptionHtml')
});
