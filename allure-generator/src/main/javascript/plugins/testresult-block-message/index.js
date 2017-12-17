import TestResultBlockMessageView from './TestResultBlockMessageView';

allure.api.addTestResultBlock(TestResultBlockMessageView, {
    order: 0,
    condition: (model) => model.has('message')
});
