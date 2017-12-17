import TestResultBlockLinksView from './TestResultBlockLinksView';

allure.api.addTestResultBlock(TestResultBlockLinksView, {
    position: 'tag',
    condition: (model) => {
        const links = model.get('links');
        return links && links.length > 0;
    }
});
