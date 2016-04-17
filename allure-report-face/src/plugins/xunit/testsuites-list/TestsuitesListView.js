import './styles.css';
import DataGridView from '../../../components/data-grid/DataGridView';
import {reduce} from 'underscore';
import StatusToggleView from '../../../components/status-toggle/StatusToggleView';
import {region} from '../../../decorators';
import settings from '../../../util/settings';
import template from './TestsuitesListView.hbs';

class TestsuitesListView extends DataGridView {
    template = template;
    settingsKey = 'xUnitSettings';

    @region('.testsuites-list__statuses')
    statuses;

    initialize({state}) {
        this.state = state;
        this.listenTo(this.state, 'change:testsuite', (m, suite) => this.highlightItem(suite));
        this.listenTo(settings, 'change:visibleStatuses', this.render);
    }

    onRender() {
        this.statuses.show(new StatusToggleView());
        this.highlightItem(this.state.get('testsuite'));
    }

    serializeData() {
        const statuses = settings.get('visibleStatuses');
        const sorting = this.getSettings();
        return {
            baseUrl: 'xUnit',
            sorting: sorting,
            time: this.collection.time,
            statistic: this.collection.statistic,
            suites: this.applySort(
                this.collection.toJSON().filter(suite => {
                    return reduce(
                        suite.statistic,
                        (visible, value, status) => visible || (statuses[status.toUpperCase()] && value > 0),
                        false
                    );
                })
            )
        };
    }
}

export default TestsuitesListView;
