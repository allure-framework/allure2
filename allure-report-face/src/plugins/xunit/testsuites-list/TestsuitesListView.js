import './styles.css';
import {LayoutView} from 'backbone.marionette';
import {reduce} from 'underscore';
import settings from '../../../util/settings';
import template from './TestsuitesListView.hbs';
import StatusToggleView from '../../../components/status-toggle/StatusToggleView';
import {colors} from '../../../util/statuses';
import {region, on} from '../../../decorators';
import 'jquery-sparkline';

class TestsuitesListView extends LayoutView {
    template = template;

    initialize({state}) {
        this.state = state;
        this.statusesSelect = new StatusToggleView();
        this.listenTo(this.state, 'change:testcase', (m, testcase) => this.highlightItem(testcase));
        this.listenTo(settings, 'change:visibleStatuses', this.render);
    }

    onDomRefresh() {
        this.$el.find('.node-stats').sparkline('html', {type: 'pie', sliceColors: colors});
    }

    onRender() {
        this.highlightItem(this.state.get('testcase'));
    }

    @on('click .node-leaf')
    onNodeClick(e) {
        this.$(e.currentTarget).parent().find('.node-branch').toggleClass('node-branch_collapsed');
    }

    @on('click .statuses-filter')
    onFilterClick(e) {
        const filter = this.$(e.currentTarget);
        filter.toggleClass('statuses-filter_active', !this.statusesSelect.isVisible());
        
        if(this.statusesSelect.isVisible()) {
            this.statusesSelect.hide();
        } else {
            this.statusesSelect.show(filter);
        }
    }

    highlightItem(uid) {
        this.$('[data-uid]').each((i, node) => {
            const el = this.$(node);
            el.toggleClass('node-leaf_active', el.data('uid') === uid);
        });
    }

    serializeData() {
        const statuses = settings.get('visibleStatuses');
        return {
            baseUrl: 'xUnit',
            time: this.collection.time,
            statistic: this.collection.statistic,
            suites: this.collection.toJSON()
                .filter(suite => {
                    return reduce(
                        suite.statistic,
                        (visible, value, status) => visible || (statuses[status] && value > 0),
                        false
                    );
                })
        };
    }
}

export default TestsuitesListView;
