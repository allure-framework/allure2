import './styles.css';
import $ from 'jquery';
import {View} from 'backbone.marionette';
import {on} from '../../decorators';
import router from '../../router';
import template from './StepsView.hbs';

export default class StepsView extends View {
    template = template;

    serializeData() {
        return {
            status: this.model.get('status'),
            time: this.model.get('time'),
            steps: this.model.get('steps'),
            baseUrl: this.options.baseUrl,
            attachments: this.model.get('attachments')
        };
    }

    @on('click .step__title_hasContent')
    onStepClick(e) {
        this.$(e.currentTarget).parent().toggleClass('step__expanded');
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
