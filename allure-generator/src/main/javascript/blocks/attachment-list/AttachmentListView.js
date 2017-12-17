import './styles.scss';
import {CollectionView, View} from 'backbone.marionette';
import {className} from '../../decorators';
import attachmentRow from './AttachmentRowView.hbs';
import {events, modelEvents, regions, tagName, ui} from '../../decorators/index';
import AttachmentView from '../../components/attachment/AttachmentView';

@className('attachment')
@tagName('li')
@ui({
    row: '.attachment__row',
    content: '.attachment__content'
})
@events({
    'click @ui.row': 'onAttachmentClick'
})
@modelEvents({
    'change:expanded': 'render'
})
@regions({
    content: '@ui.content'
})
class AttachmentRowView extends View {
    template = attachmentRow;

    onAttachmentClick() {
        const expanded = this.model.get('expanded');
        this.model.set('expanded', !expanded);
        if (!expanded) {
            const attachmentView = new AttachmentView({
                attachment: {
                    id: this.model.id,
                    type: this.model.get('type'),
                    source: this.model.get('source')
                }
            });
            this.showChildView('content', attachmentView);
        }
    }

    templateContext() {
        return {
            cls: this.className
        };
    }
}

@className('attachment')
@tagName('li')
class EmptyView extends View {
    template = () => 'There are no attachments present';
}

@className('attachments')
@tagName('ul')
class AttachmentListView extends CollectionView {
    childView = AttachmentRowView;
    emptyView = EmptyView;
}

export default AttachmentListView;