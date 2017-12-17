import './styles.scss';
import {CollectionView, View} from 'backbone.marionette';
import stepRow from './StepRowView.hbs';
import {className} from '../../decorators';
import {events, modelEvents, regions, tagName, ui} from '../../decorators/index';
import {Collection, Model} from 'backbone';
import AttachmentListView from '../attachment-list/AttachmentListView';
import ParameterTableView from '../parameter-table/ParameterTableView';
import StatusDetailsView from '../status-details/StatusDetailsView';

@className('step')
@tagName('li')
@ui({
    step: '.step__row_hasContent',
    parameters: '.step__parameters',
    steps: '.step__steps',
    attachments: '.step__attachments',
    message: '.step__message',
})
@regions({
    parameters: '@ui.parameters',
    steps: '@ui.steps',
    attachments: '@ui.attachments',
    message: '@ui.message',
})
@events({
    'click @ui.step': 'onStepClick'
})
@modelEvents({
    'change:expanded': 'render'
})
class StepRowView extends View {
    template = stepRow;

    onStepClick() {
        const expanded = this.model.get('expanded');
        this.model.set('expanded', !expanded);
        if (!expanded) {
            const parameters = this.model.get('parameters') || [];
            if (parameters.length) {
                this.showChildView('parameters', new ParameterTableView({collection: new Collection(parameters)}));
            }

            const steps = this.model.get('steps') || [];
            if (steps.length) {
                this.showChildView('steps', new StepListView({collection: new Collection(steps)}));
            }

            const attachments = this.model.get('attachments') || [];
            if (attachments.length) {
                this.showChildView('attachments', new AttachmentListView({collection: new Collection(attachments)}));
            }

            const leaf = this.model.get('leaf');
            const message = this.model.get('message');
            if (leaf && message) {
                const statusDetails = new Model({
                    message: message,
                    trace: this.model.get('trace'),
                    status: this.model.get('status')
                });
                this.showChildView('message', new StatusDetailsView({model: statusDetails}));
            }
        }
    }

    templateContext() {
        return {
            cls: this.className,
        };
    }
}

@className('step')
@tagName('li')
class EmptyView extends View {
    template = () => 'There are no steps present';
}

@className('steps')
@tagName('ul')
class StepListView extends CollectionView {
    childView = StepRowView;
    emptyView = EmptyView;
}

export default StepListView;