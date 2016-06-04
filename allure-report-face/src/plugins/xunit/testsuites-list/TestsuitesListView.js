import './styles.css';
import DataGridView from '../../../components/data-grid/DataGridView';
import {reduce} from 'underscore';
import {region} from '../../../decorators';
import settings from '../../../util/settings';
import template from './TestsuitesListView.hbs';
import {colors} from '../../../util/statuses'
import 'jquery-sparkline';

class TestsuitesListView extends DataGridView {
    template = template;
    settingsKey = 'xUnitSettings';


    initialize({state}) {
        this.state = state;
        this.listenTo(this.state, 'change:testsuite', (m, suite) => this.highlightItem(suite));
        this.listenTo(settings, 'change:visibleStatuses', this.render);
    }

    onDomRefresh() {
        this.$el.find('.node-stats').sparkline('html', {type: 'bar', colorMap: colors});
    }

    onRender() {
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
                this.collection.toJSON()
                    .filter(suite => {
                        return reduce(
                            suite.statistic,
                            (visible, value, status) => visible || (statuses[status] && value > 0),
                            false
                        );
                    })
            )
        };
    }
}

export default TestsuitesListView;
