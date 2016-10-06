import './styles.css';
import d3 from 'd3';
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
        this.type = attachmentType(this.attachment.type);
        this.sourceUrl = 'data/attachments/' + this.attachment.source;
    }

    onRender() {
        if(this.needsFetch() && !this.content) {
            this.loadContent().then(this.render);
        } else if(this.type === 'code') {
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
            if(this.type === 'csv') {
                this.content = d3.csv.parseRows(responseText);
            } else if(this.type === 'uri') {
                this.content = responseText.split('\n')
                    .map(line => line.trim())
                    .filter(line => line.length > 0)
                    .map(line => ({
                        comment: line.startsWith('#'),
                        text: line
                    }));
            } else {
                this.content = responseText;
            }
        });
    }

    needsFetch() {
        return ['text', 'code', 'csv', 'uri'].indexOf(this.type) > -1;
    }

    serializeData() {
        return {
            type: this.type,
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
