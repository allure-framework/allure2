import {View} from 'backbone.marionette';
import {className, regions, ui} from '../../decorators';
import template from './TestResultBlockExecutionView.hbs';
import {Model} from 'backbone';
import ExecutionView from '../../components/execution/ExecutionView';

@className('pane__section')
@ui({
    'content': '.testresult-block-execution__content'
})
@regions({
    'content': '@ui.content'
})
class TestResultBlockExecutionView extends View {
    template = template;

    onRender() {
        const testStage = this.model.get('testStage');
        if (testStage) {
            const execution = new Model({
                steps: testStage.steps,
                attachments: testStage.attachments
            });
            this.showChildView('content', new ExecutionView({model: execution}));
        }
    }
}

export default TestResultBlockExecutionView;
