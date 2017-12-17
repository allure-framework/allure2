import TestResultBlockTagsView from './TestResultBlockTagsView';

allure.api.addTestResultBlock(TestResultBlockTagsView, {
    position: 'tag',
    condition: (model) => {
        const extra = model.get('extra');
        return extra && extra.tags && extra.tags.length > 0;
    }
});
