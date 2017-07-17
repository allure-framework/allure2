import './styles.scss';
import {View} from 'backbone.marionette';
import {regions, behavior, className} from '../../decorators';
import template from './TestResultView.hbs';
import {Model} from 'backbone';
import TestResultOverviewView from '../testresult-overview/TestResultOverviewView';
import TestResultExecutionView from '../testresult-execution/TestResultExecutionView';
import TestResultHistoryView from '../testresult-history/TestResultHistoryView';
import ErrorSplashView from '../error-splash/ErrorSplashView';

const subViews = [
    {tab: '', name: 'Overview', View: TestResultOverviewView},
    {tab: 'execution', name: 'Execution', View: TestResultExecutionView},
    {tab: 'history', name: 'History', View: TestResultHistoryView}
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
        this.state = new Model();
        this.tabName =  this.routeState.get('testResultTab') || '';
        this.listenTo(this.routeState, 'change:testResultTab', (_, tabName) => this.onTabChange(tabName));
    }

    onRender() {
        const subView = subViews.find(view => view.tab === this.tabName);
        this.showChildView('content', !subView
            ? new ErrorSplashView({code: 404, message: `Tab "${this.tabName}" not found`})
            : new subView.View(this.options)
        );
    }

    onTabChange(tabName) {
        this.tabName = tabName || '';
        this.render();
    }

    templateContext() {
        const {baseUrl} = this.options;
        return {
            cls: this.className,
            statusName: `status.${this.model.get('status')}`,
            links: subViews.map(view => {
                return {
                    href: `${baseUrl}/${view.tab}`,
                    name: view.name,
                    active: view.tab === this.tabName
                };
            })
        };
    }
}

export default TestResultView;
