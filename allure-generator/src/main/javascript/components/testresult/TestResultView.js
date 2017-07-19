import './styles.scss';
import {View} from 'backbone.marionette';
import {regions, behavior, className} from '../../decorators';
import template from './TestResultView.hbs';
import {Model} from 'backbone';
import TestResultOverviewView from '../testresult-overview/TestResultOverviewView';
import TestResultExecutionView from '../testresult-execution/TestResultExecutionView';
import ErrorSplashView from '../error-splash/ErrorSplashView';
import pluginsRegistry from '../../util/pluginsRegistry';

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
        this.state = new Model();
        this.tabName =  this.routeState.get('testResultTab') || '';
        this.listenTo(this.routeState, 'change:testResultTab', (_, tabName) => this.onTabChange(tabName));
    }

    onRender() {
        const subView = this.tabs.find(view => view.id === this.tabName);
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
