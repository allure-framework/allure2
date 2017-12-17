import TestResultBlockParametersView from './TestResultBlockParametersView';

allure.api.addTestResultBlock(TestResultBlockParametersView, {
    position: 'before',
    condition: (model) => {
        const parameters = model.get('parameters');
        return parameters && parameters.length > 0;
    }
});
