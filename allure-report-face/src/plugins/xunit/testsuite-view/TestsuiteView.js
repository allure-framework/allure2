import {LayoutView} from 'backbone.marionette';
import {region, behavior} from '../../../decorators';
import template from './TestsuiteView.hbs';
import TestcaseTableView from '../../../components/testcase-table/TestcaseTableView';

@behavior('TooltipBehavior', {position: 'bottom'})
class TestsuiteView extends LayoutView {
    template = template;

    @region('.testsuite__testcases')
    testcases;

    initialize({testsuite, state}) {
        this.state = state;
        this.listenTo(this.state, 'change:testcase', (m, testcase) => this.showTestcase(testcase));
        this.model = testsuite;
    }

    onRender() {
        this.testcaseTable = new TestcaseTableView({
            testCases: this.model.get('testCases'),
            currentCase: this.state.get('testcase'),
            baseUrl: 'xUnit/' + this.state.get('testsuite')
        });
        this.testcases.show(this.testcaseTable);
    }

    showTestcase(testcase) {
        this.testcaseTable.highlightItem(testcase);
    }

    serializeData() {
        return Object.assign({
            baseUrl: this.options.baseUrl
        }, super.serializeData());
    }
}

export default TestsuiteView;
