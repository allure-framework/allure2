import TestResultBlockOwnerView from './TestResultBlockOwnerView';

allure.api.addTestResultBlock(TestResultBlockOwnerView, {
    position: 'tag',
    condition: (model) => {
        const extra = model.get('extra');
        return extra && extra.owner;
    }
});