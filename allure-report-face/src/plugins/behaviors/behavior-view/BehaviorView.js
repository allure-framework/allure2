import {LayoutView} from 'backbone.marionette';
import {region, behavior} from '../../../decorators';
import template from './BehaviorView.hbs';
import TestcaseTableView from '../../../components/testcase-table/TestcaseTableView';

@behavior('TooltipBehavior', {position: 'bottom'})
class BehaviorView extends LayoutView {
    template = template;

    @region('.behavior__testcases')
    testcases;

    initialize({state}) {
        this.state = state;
        this.listenTo(this.state, 'change:testcase', (m, testcase) => this.showTestcase(testcase));
    }

    onRender() {
        this.testcaseTable = new TestcaseTableView({
            testCases: this.model.get('testCases'),
            currentCase: this.state.get('testcase'),
            baseUrl: 'behaviors/' + this.state.get('behavior')
        });
        this.testcases.show(this.testcaseTable);
    }

    showTestcase(testcase) {
       this.testcaseTable.highlightItem(testcase);
    }

    serializeData() {
        return Object.assign({
            baseUrl: 'behaviors'
        }, super.serializeData());
    }
}

export default BehaviorView;
