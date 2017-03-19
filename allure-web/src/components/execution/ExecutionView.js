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
        const countKey = `${key}Count`;
        steps.forEach(step => {
            this.calculateSteps(step.steps, key);
            step[countKey] = step[key].length + step.steps.reduce((count, cur) => count + cur[countKey], 0);
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

    @on('click .parameters__table_cell')
    onParameterClick(e) {
        this.$(e.target).siblings().addBack().toggleClass('line-ellipsis');
    }
}

export default ExecutionView;
