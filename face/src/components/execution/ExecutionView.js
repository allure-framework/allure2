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
        router.toUrl(this.options.baseUrl + '/' + attachmentUid);
    }

    @on('click .attachment-row__link')
    onAttachmentFileClick(e) {
        e.stopPropagation();
    }
}

export default ExecutionView;