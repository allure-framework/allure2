import './styles.css';
import {View} from 'backbone.marionette';
import template from './ExecutionView.hbs';
import {className} from '../../decorators';
import {makeArray} from '../../util/arrays';
import {on} from '../../decorators';
import router from '../../router';
import $ from 'jquery';

@className('execution')
class ExecutionView extends View {
    template = template;

    initialize({state}) {
        this.state = state;
        this.listenTo(this.state, 'change:attachment', this.highlightSelectedAttachment, this);
    }

    onRender() {
        this.highlightSelectedAttachment();
    }

    highlightSelectedAttachment() {
        const currentAttachment = this.state.get('attachment');
        this.$('.attachment-row').removeClass('attachment-row_selected');

        const attachmentEl = this.$(`.attachment-row[data-uid="${currentAttachment}"]`);
        attachmentEl.addClass('attachment-row_selected');
        attachmentEl.parents('.step').addClass('step_expanded');
    }

    calculateSteps(steps, key) {
        return steps.map(step => {
            var children = this.calculateSteps(step.steps, key);
            step[key + 'Count'] = step[key].length + children.reduce((count, cur) => { return count + cur[key + 'Count'];}, 0);
            step.steps = children;
            return step;
        });
    }

    getStage(executionStage) {
        var stages = makeArray(this.model.get(executionStage));
        this.calculateSteps(stages, 'steps');
        this.calculateSteps(stages, 'attachments');
        return stages;
    }

    serializeData() {
        const before = this.getStage('beforeStages');
        const test = this.getStage('testStage');
        const after = this.getStage('afterStages');
        return {
            hasContent: before.length + test.length + after.length > 0,
            before: before,
            test: test,
            after: after,
            baseUrl: this.options.baseUrl
        };
    }

    @on('click .step__title_hasContent')
    onStepClick(e) {
        this.$(e.currentTarget).parent().toggleClass('step_expanded');
    }

    @on('click .attachment-row')
    onAttachmentClick(e) {
        const attachmentUid = $(e.currentTarget).data('uid');
        router.toUrl(this.options.baseUrl + '/' + attachmentUid);
    }

    @on('click .attachment-row__link')
    onAttachmentFileClick(e) {
        e.stopPropagation();
    }
}

export default ExecutionView;
