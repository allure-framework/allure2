import './styles.css';
import {View} from 'backbone.marionette';
import settings from '../../util/settings';
import template from './TreeView.hbs';
import StatusToggleView from '../status-toggle/StatusToggleView';
import NodeSorterView from '../node-sorter/NodeSorterView';
import {on, regions} from '../../decorators';
import {behavior} from '../../decorators/index';

@behavior('TooltipBehavior', {position: 'bottom'})
@regions({sorter: '.tree__sorter'})
class TreeView extends View {
    template = template;

    initialize({state, tabName, baseUrl}) {
        this.state = state;
        this.baseUrl = baseUrl;
        this.tabName = tabName;
        this.statusesKey = tabName + '.visibleStatuses';
        this.sorterSettingsKey = tabName + '.treeSorting';
        this.statusesSelect = new StatusToggleView({statusesKey: this.statusesKey});
        this.nodeSorter = new NodeSorterView({sorterSettingsKey: this.sorterSettingsKey});
        this.listenTo(this.state, 'change:testcase', (m, testcase) => this.restoreState(testcase));
        this.listenTo(settings, 'change:' + this.statusesKey, this.render);
        this.listenTo(settings, 'change:' + this.sorterSettingsKey, this.render);
        this.listenTo(settings, 'change:showGroupInfo', this.render);
    }

    onRender() {
        this.restoreState();
        this.showChildView('sorter', new NodeSorterView({sorterSettingsKey: this.sorterSettingsKey}));
    }

    onDestroy() {
        this.statusesSelect.hide();
    }

    @on('click .node__title')
    onNodeClick(e) {
        this.$(e.currentTarget).parent().toggleClass('node__expanded');
        const uid = this.$(e.currentTarget).data('uid');
        if (this.state.has(uid)) {
            delete this.state.unset(uid, true);
        } else {
            this.state.set(uid, true);
        }
    }

    @on('click .tree__statuses')
    onFilterClick(e) {
        const filter = this.$(e.currentTarget);
        if(this.statusesSelect.isVisible()) {
            this.statusesSelect.hide();
        } else {
            this.statusesSelect.show(filter);
        }
    }

    @on('click .tree__info')
    onInfoClick() {
        const show = settings.get('showGroupInfo');
        settings.save('showGroupInfo', !show);
    }

    restoreState() {
        this.$('[data-uid]').each((i, node) => {
            const el = this.$(node);
            el.toggleClass('node__title_active', el.data('uid') === this.state.get('testcase'));
            el.toggleClass('node__expanded', (this.state.has(el.data('uid'))));
        });
        this.$('.node__title_active').parents('.node').toggleClass('node__expanded', true);
    }

    serializeData() {
        const statuses = settings.getVisibleStatuses(this.statusesKey);
        const sorter = this.nodeSorter.getSorter();
        const showGroupInfo = settings.get('showGroupInfo');

        return {
            baseUrl: this.baseUrl,
            showGroupInfo: showGroupInfo,
            time: this.collection.time,
            statistic: this.collection.statistic,
            tabName: this.tabName,
            items: this.filterNodes(statuses, sorter, this.collection.toJSON()),
        };
    }

    filterNodes(statuses, sorter, nodes) {
        return nodes
            .map(item => this.mapNode(statuses, sorter, item))
            .filter(item => item.type === 'TestCaseNode' ? statuses[item.status] : item.children.length > 0)
            .sort(sorter);
    }

    mapNode(statuses, sorter, node) {
        if (node.type === 'TestCaseNode') {
            return node;
        }
        return Object.assign({}, node, {
            children: this.filterNodes(statuses, sorter, node.children)
        });
    }
}

export default TreeView;
