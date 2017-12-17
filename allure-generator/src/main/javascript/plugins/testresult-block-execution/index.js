import TestResultBlockExecutionView from './TestResultBlockExecutionView';

allure.api.addTestResultBlock(TestResultBlockExecutionView, {
    order: 50,
    condition: (model) => {
        if (!model.has('testStage')) {
            return false;
        }
        const testStage = model.get('testStage');
        const steps = testStage.steps || [];
        const attachments = testStage.attachments || [];
        return steps.length + attachments.length > 0;
    }
});
