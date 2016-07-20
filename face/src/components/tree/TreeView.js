import './styles.css';
import {LayoutView} from 'backbone.marionette';
import settings from '../../util/settings';
import template from './TreeView.hbs';
import StatusToggleView from '../status-toggle/StatusToggleView';
import {on} from '../../decorators';

class TreeView extends LayoutView {
    template = template;

    initialize({state, tabName, baseUrl}) {
        this.state = state;
        this.baseUrl = baseUrl;
        this.tabName = tabName;
        this.statusesSelect = new StatusToggleView();
        this.listenTo(this.state, 'change:testcase', (m, testcase) => this.highlightItem(testcase));
        this.listenTo(settings, 'change:visibleStatuses', this.render);
    }

    onDomRefresh() {
        this.$('.node__title_active').parents('.node').toggleClass('node__expanded', true);
    }

    onRender() {
        this.highlightItem(this.state.get('testcase'));
    }

    onDestroy() {
        this.statusesSelect.hide();
    }

    @on('click .node__title')
    onNodeClick(e) {
        this.$(e.currentTarget).parent().toggleClass('node__expanded');
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
            el.toggleClass('node__title_active', el.data('uid') === uid);
        });
    }

    serializeData() {
        const statuses = settings.get('visibleStatuses');
        return {
            baseUrl: this.baseUrl,
            time: this.collection.time,
            statistic: this.collection.statistic,
            tabName: this.tabName,
            items: this.filterNodes(statuses, this.collection.toJSON())
        };
    }

    filterNodes(statuses, nodes) {
        return nodes
            .map(item => this.mapNode(statuses, item))
            .filter(item => item.type === 'TestCaseNode' ? statuses[item.status] : item.children.length > 0);
    }

    mapNode(statuses, node) {
        if (node.type === 'TestCaseNode') {
            return node;
        }
        return Object.assign({}, node, {
            children: this.filterNodes(statuses, node.children)
        });
    }
}

export default TreeView;
