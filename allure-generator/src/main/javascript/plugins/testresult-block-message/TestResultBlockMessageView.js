import {View} from 'backbone.marionette';
import {className, regions, ui} from '../../decorators';
import StatusDetailsView from '../../blocks/status-details/StatusDetailsView';

@className('pane__section')
@ui({
    'content': '.testresult-block-message__content'
})
@regions({
    'content': '@ui.content'
})
class TestResultBlockMessageView extends View {
    template = () => '<div class="testresult-block-message__content"></div>';

    onRender() {
        this.showChildView('content', new StatusDetailsView({model: this.model}));
    }
}

export default TestResultBlockMessageView;
