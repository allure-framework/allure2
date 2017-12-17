import {View} from 'backbone.marionette';
import template from './ExecutionView.hbs';
import {className} from '../../decorators';
import {regions, ui} from '../../decorators/index';
import {Collection} from 'backbone';
import AttachmentListView from '../../blocks/attachment-list/AttachmentListView';
import StepListView from '../../blocks/step-list/StepListView';

@className('execution')
@ui({
    steps: '.execution__steps',
    attachments: '.execution__attachments',
})
@regions({
    steps: '@ui.steps',
    attachments: '@ui.attachments',
})
class ExecutionView extends View {
    template = template;

    onRender() {
        const steps = this.model.get('steps') || [];
        if (steps.length) {
            this.showChildView('steps', new StepListView({collection: new Collection(steps)}));
        }

        const attachments = this.model.get('attachments') || [];
        if (attachments.length) {
            this.showChildView('attachments', new AttachmentListView({collection: new Collection(attachments)}));
        }
    }

    templateContext() {
        const steps = this.model.get('steps') || [];
        const attachments = this.model.get('attachments') || [];

        return {
            cls: this.className,
            hasContent: steps.length + attachments.length > 0,
            emptyMessage: this.options.emptyMessage || 'There is no execution present'
        };
    }
}

export default ExecutionView;
