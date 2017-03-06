import './styles.css';
import DataGridView from '../data-grid/DataGridView';
import {regions} from '../../decorators';
import settings from '../../util/settings';
import StatusToggleView from '../status-toggle/StatusToggleView';
import template from './TestcaseTableView.hbs';

@regions({
    statuses: '.testcase-table__statuses'
})
class TestcaseTableView extends DataGridView {
    template = template;
    settingsKey = 'testCaseSorting';

    initialize() {
        this.statusesKey = 'testcaseTableStatuses';
        if (!settings.get(this.statusesKey)) {
            settings.save(this.statusesKey, settings.get('visibleStatuses'));
        }
        this.listenTo(settings, 'change:' + this.statusesKey, this.render);
        this.testCases = this.options.testCases.map((testcase, index) => Object.assign(testcase, {index: index + 1}));
    }

    onRender() {
        // this.highlightItem(this.options.currentCase);
        this.showChildView('statuses', new StatusToggleView({statusesKey: this.statusesKey}));
    }

    serializeData() {
        const statuses = settings.get(this.statusesKey);
        const sorting = this.getSettings();
        return {
            baseUrl: this.options.baseUrl,
            sorting: sorting,
            totalCount: this.options.testCases.length,
            testCases: this.applySort(this.testCases).filter(({status}) => statuses[status])
        };
    }
}

export default TestcaseTableView;
