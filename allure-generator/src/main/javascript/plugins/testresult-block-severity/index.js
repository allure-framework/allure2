import TestResultBlockSeverityView from './TestResultBlockSeverityView';

allure.api.addTestResultBlock(TestResultBlockSeverityView, {
    position: 'tag',
    condition: (model) => {
        const extra = model.get('extra');
        return extra && extra.severity;
    }
});