import {View} from 'backbone.marionette';
import {region, behavior} from '../../../decorators';
import TestcaseTableView from '../../../components/testcase-table/TestcaseTableView';
import template from './DefectView.hbs';

@behavior('TooltipBehavior', {position: 'bottom'})
class DefectView extends View {
    template = template;

    @region('.defect__table')
    testcases;

    initialize({state}) {
        this.state = state;
        this.listenTo(this.state, 'change:testcase', (m, testcase) => this.showTestcase(testcase));
    }

    onRender() {
        this.testcaseTable = new TestcaseTableView({
            testCases: this.model.get('testCases'),
            currentCase: this.state.get('testcase'),
            baseUrl: 'defects/' + this.state.get('defect')
        });
        this.showChildView('testcases', this.testcaseTable);
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

export default DefectView;
