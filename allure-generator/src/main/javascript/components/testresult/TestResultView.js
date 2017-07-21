import './styles.scss';
import {View} from 'backbone.marionette';
import {regions, behavior, className} from '../../decorators';
import template from './TestResultView.hbs';
import TestResultOverviewView from '../testresult-overview/TestResultOverviewView';
import TestResultExecutionView from '../testresult-execution/TestResultExecutionView';
import ErrorSplashView from '../error-splash/ErrorSplashView';
import pluginsRegistry from '../../util/pluginsRegistry';
import ModalView from '../modal/ModalView';
import AttachmentView from '../attachment/AttachmentView';
import translate from '../../helpers/t';

const subViews = [
    {id: '', name: 'testResult.overview.name', View: TestResultOverviewView},
    {id: 'execution', name: 'testResult.execution.name', View: TestResultExecutionView}
];

@className('test-result')
@behavior('TooltipBehavior', {position: 'left'})
@behavior('ClipboardBehavior')
@regions({
    content: '.test-result__content'
})
class TestResultView extends View {
    template = template;

    initialize({routeState}) {
        this.routeState = routeState;
        this.tabs = subViews.concat(pluginsRegistry.testResultTabs);
        this.tabName =  this.routeState.get('testResultTab') || '';
        this.listenTo(this.routeState, 'change:testResultTab', (_, tabName) => this.onTabChange(tabName));
        this.listenTo(this.routeState, 'change:attachment', (_, uid) => this.onShowAttachment(uid));
    }

    onRender() {
        const subView = this.tabs.find(view => view.id === this.tabName);
        this.showChildView('content', !subView
            ? new ErrorSplashView({code: 404, message: `Tab "${this.tabName}" not found`})
            : new subView.View(this.options)
        );

        const attachment = this.routeState.get('attachment');
        if (attachment) {
            this.onShowAttachment(attachment);
        }
    }

    onTabChange(tabName) {
        this.tabName = tabName || '';
        this.render();
    }

    onShowAttachment(uid) {
        const attachment = this.model.getAttachment(uid);
        const modalView = new ModalView({
            childView: attachment
                ? new AttachmentView({attachment, fullScreen: true})
                : new ErrorSplashView({code: 404, message: translate('errors.missedAttachment')}),
            title: attachment
                ? attachment.name || attachment.source
                : translate('errors.notFound')
        });
        modalView.show();
    }

    templateContext() {
        const {baseUrl} = this.options;
        return {
            cls: this.className,
            statusName: `status.${this.model.get('status')}`,
            links: this.tabs.map(view => {
                return {
                    href: `${baseUrl}/${view.id}`,
                    name: view.name,
                    active: view.id === this.tabName
                };
            })
        };
    }
}

export default TestResultView;
