import './styles.css';
import {LayoutView} from 'backbone.marionette';
import {reduce} from 'underscore';
import settings from '../../../util/settings';
import template from './TestsuitesListView.hbs';
import {colors} from '../../../util/statuses';
import {region, on} from '../../../decorators';
import 'jquery-sparkline';

class TestsuitesListView extends LayoutView {
    template = template;
    settingsKey = 'xUnitSettings';
    templateHelpers = function(){
        return {
            foo: () => {  
                console.log(this);
                return this.state.get('testcase'); 
            }
        }
    };

    initialize({state}) {
        this.state = state;
        this.listenTo(this.state, 'change:testcase', (m, testcase) => this.highlightItem(testcase));
        this.listenTo(settings, 'change:visibleStatuses', this.render);
    }

    onDomRefresh() {
        this.$el.find('.node-stats').sparkline('html', {type: 'bar', colorMap: colors});
    }

    onRender() {
        this.highlightItem(this.state.get('testcase'));
    }

    @on('click .node-leaf')
    onNodeClick(e) {
        this.$(e.currentTarget).parent().find('.node-branch').toggleClass('node-branch_collapsed');
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
