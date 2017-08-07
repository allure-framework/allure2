import './styles.scss';
import highlight from '../../util/highlight';
import {View} from 'backbone.marionette';
import $ from 'jquery';
import router from '../../router';
import {className, on, behavior} from '../../decorators';
import attachmentType from '../../util/attachmentType';
import template from './AttachmentView.hbs';


@className('attachment')
@behavior('TooltipBehavior', {position: 'bottom'})
class AttachmentView extends View {
    template = template;

    initialize({attachment}) {
        this.attachment = attachment;
        this.attachmentInfo = attachmentType(this.attachment.type);
        this.sourceUrl = 'data/attachments/' + this.attachment.source;
    }

    onRender() {
        if(this.needsFetch() && !this.content) {
            this.loadContent().then(this.render);
        } else if(this.attachmentInfo.type === 'code') {
            const codeBlock = this.$('.attachment__code');
            codeBlock.addClass('language-' + this.attachment.type.split('/').pop());
            highlight.highlightBlock(codeBlock[0]);
        }
    }

    @on('click .attachment__media')
    onImageClick() {
        const expanded = router.getUrlParams().expanded === 'true' ? null : true;
        router.setSearch({expanded});
    }

    loadContent() {
        return $.ajax(this.sourceUrl, {dataType: 'text'}).then((responseText) => {
            const parser = this.attachmentInfo.parser;
            this.content = parser(responseText);
        });
    }

    needsFetch() {
        return 'parser' in this.attachmentInfo;
    }

    serializeData() {
        return {
            type: this.attachmentInfo.type,
            content: this.content,
            sourceUrl: this.sourceUrl,
            attachment: this.attachment,
            route: {
                baseUrl: this.options.baseUrl
            }
        };
    }
}

export default AttachmentView;
