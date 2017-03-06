import './styles.css';
import {View} from 'backbone.marionette';
import settings from '../../util/settings';
import template from './TreeView.hbs';
import StatusToggleView from '../status-toggle/StatusToggleView';
import {on} from '../../decorators';
import {behavior} from '../../decorators/index';

@behavior('TooltipBehavior', {position: 'bottom'})
class TreeView extends View {
    template = template;

    initialize({state, tabName, baseUrl}) {
        this.state = state;
        this.baseUrl = baseUrl;
        this.tabName = tabName;
        this.statusesKey = tabName + 'visibleStatuses';
        if (!settings.get(this.statusesKey)) {
            settings.save(this.statusesKey, settings.get('visibleStatuses'));
        }
        this.statusesSelect = new StatusToggleView({statusesKey: this.statusesKey});
        this.listenTo(this.state, 'change:testcase', (m, testcase) => this.highlightItem(testcase));
        this.listenTo(settings, 'change:' + this.statusesKey, this.render);
        this.listenTo(settings, 'change:showGroupInfo', this.render);
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

    highlightItem(uid) {
        this.$('[data-uid]').each((i, node) => {
            const el = this.$(node);
            el.toggleClass('node__title_active', el.data('uid') === uid);
        });
    }

    serializeData() {
        const statuses = settings.get(this.statusesKey);
        const showGroupInfo = settings.get('showGroupInfo');
        return {
            baseUrl: this.baseUrl,
            showGroupInfo: showGroupInfo,
            time: this.collection.time,
            statistic: this.collection.statistic,
            tabName: this.tabName,
            items: this.filterNodes(statuses, this.collection.toJSON())
        };
    }

    filterNodes(statuses, nodes) {
        return nodes
            .map(item => this.mapNode(statuses, item))
            .filter(item => item.type === 'TestCaseNode' ? statuses[item.status] : item.children.length > 0)
            .sort((a, b) => a.name < b.name ? 1 : -1);
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
