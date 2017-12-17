import TestResultBlockCategoryView from './TestResultBlockCategoryView';

allure.api.addTestResultBlock(TestResultBlockCategoryView, {
    order: 10,
    condition: (model) => {
        const extra = model.get('extra');
        return extra && extra.categories && extra.categories.length > 0;
    }
});
