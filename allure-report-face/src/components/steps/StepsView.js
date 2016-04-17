import './styles.css';
import $ from 'jquery';
import {ItemView} from 'backbone.marionette';
import {on} from '../../decorators';
import router from '../../router';
import template from './StepsView.hbs';

export default class StepsView extends ItemView {
    template = template;

    fillStep(step) {
        return Object.assign({}, step, {
            hasContent: step.steps.length > 0 || step.attachments.length > 0,
            steps: step.steps.map(this.fillStep, this)
        });
    }

    serializeData() {
        return {
            status: this.model.get('status'),
            time: this.model.get('time'),
            steps: this.model.get('steps').map(this.fillStep, this),
            baseUrl: this.options.baseUrl,
            attachments: this.model.get('attachments')
        };
    }

    @on('click .step__title')
    onStepClick(e) {
        const el = $(e.currentTarget);
        if(el.hasClass('step__title_expanded')) {
            el.parent().find('.step__title_expanded').removeClass('step__title_expanded');
        } else {
            el.addClass('step__title_expanded');
        }
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
