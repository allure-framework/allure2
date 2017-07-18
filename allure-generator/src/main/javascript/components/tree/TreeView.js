import './styles.scss';
import {View} from 'backbone.marionette';
import settings from '../../util/settings';
import hotkeys from '../../util/hotkeys';
import template from './TreeView.hbs';
import {behavior, className, on, regions} from '../../decorators';
import getComparator from '../../data/tree/comparator';
import {byStatuses} from '../../data/tree/filter';
import NodeSorterView from '../node-sorter/NodeSorterView';
import StatusToggleView from '../status-toggle/StatusToggleView';

@className('tree')
@behavior('TooltipBehavior', {position: 'bottom'})
@regions({
    sorter: '.tree__sorter',
    filter: '.tree__filter'
})
class TreeView extends View {
    template = template;

    initialize({routeState, tabName, baseUrl}) {
        this.routeState = routeState;
        this.baseUrl = baseUrl;
        this.tabName = tabName;
        this.statusesKey = tabName + '.visibleStatuses';
        this.sorterSettingsKey = tabName + '.treeSorting';
        this.listenTo(this.routeState, 'change:treeNode', (_, treeNode) => this.changeSelectedNode(treeNode));
        this.listenTo(settings, 'change:' + this.statusesKey, this.render);
        this.listenTo(settings, 'change:' + this.sorterSettingsKey, this.render);
        this.listenTo(settings, 'change:showGroupInfo', this.render);
        this.listenTo(hotkeys, 'key:up', this.onKeyUp, this);
        this.listenTo(hotkeys, 'key:down', this.onKeyDown, this);
    }

    onBeforeRender() {
        const visibleStatuses = settings.getVisibleStatuses(this.statusesKey);
        const filter = byStatuses(visibleStatuses);
        const sortSettings = settings.getTreeSorting(this.sorterSettingsKey);
        const sorter = getComparator(sortSettings);
        this.collection.applyFilterAndSorting(filter, sorter);
    }

    onRender() {
        this.showChildView('sorter', new NodeSorterView({
            sorterSettingsKey: this.sorterSettingsKey
        }));
        this.showChildView('filter', new StatusToggleView({
            statusesKey: this.statusesKey,
            statistic: this.collection.statistic
        }));
    }

    changeSelectedNode(treeNode) {
        const previous = this.routeState.previous('treeNode');
        if (previous) {
            const el = this.findElement(previous);
            el.toggleClass('node__title_active', false);
        }

        const el = this.findElement(treeNode);
        el.toggleClass('node__title_active', true);
        this.$('.node__title_active').parents('.node').toggleClass('node__expanded', true);
    }

    findElement(treeNode) {
        if (treeNode.testResult) {
            return this.$(`[data-uid='${treeNode.testResult}'][data-parentUid='${treeNode.testGroup}']`);
        } else {
            return this.$(`[data-uid='${treeNode.testGroup}']`);
        }
    }

    @on('click .node__title')
    onNodeClick(e) {
        this.$(e.currentTarget).parent().toggleClass('node__expanded');
    }

    @on('click .tree__info')
    onInfoClick() {
        const show = settings.get('showGroupInfo');
        settings.save('showGroupInfo', !show);
    }

    templateContext() {
        return {
            cls: this.className,
            baseUrl: this.baseUrl,
            showGroupInfo: settings.get('showGroupInfo'),
            time: this.collection.time,
            statistic: this.collection.statistic,
            uid: this.collection.uid,
            tabName: this.tabName,
            items: this.collection.toJSON(),
            shownCases: 0,
            totalCases: 0,
            filtered: false
        };
    }
}

export default TreeView;
