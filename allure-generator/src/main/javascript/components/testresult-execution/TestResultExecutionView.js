import $ from 'jquery';
import './styles.scss';
import AttachmentView from '../attachment/AttachmentView';
import template from './TestResultExecutionView.hbs';
import {className, on} from '../../decorators';
import {makeArray} from '../../util/arrays';
import {Model} from 'backbone';
import {View} from 'backbone.marionette';


@className('test-result-execution')
class TestResultExecutionView extends View {
    template = template;

    initialize() {
        this.state = new Model();
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

    serializeData() {
        const before = makeArray(this.model.get('beforeStages'));
        const test = makeArray(this.model.get('testStage'));
        const after = makeArray(this.model.get('afterStages'));
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
        const name = `attachment__${attachmentUid}`;

        if($(e.currentTarget).hasClass('attachment-row_selected')) {
            this.getRegion(name).destroy();
        } else {
            this.addRegion(name, {el: this.$(`.${name}`)});
            this.getRegion(name).show(new AttachmentView({
                baseUrl: this.options.baseUrl,
                attachment: this.model.getAttachment(attachmentUid),
                state: this.state
            }));
        }
        this.$(e.currentTarget).toggleClass('attachment-row_selected');
    }

    @on('click .attachment-row__link')
    onAttachmentFileClick(e) {
        e.stopPropagation();
    }

    @on('click .parameters__table_cell')
    onParameterClick(e) {
        this.$(e.target).siblings().addBack().toggleClass('line-ellipsis');
    }

    @on('click .status-details__trace-toggle')
    onStacktraceClick(e) {
        this.$(e.currentTarget).closest('.status-details').toggleClass('status-details__expanded');
    }
}

export default TestResultExecutionView;
