import TestResultBlockHistoryView from './TestResultBlockHistoryView';

allure.api.addTestResultBlock(TestResultBlockHistoryView, {
    position: 'tag',
    condition: (model) => {
        const extra = model.get('extra');
        return extra && extra.history;
    }
});