import './styles.scss';
import {View} from 'backbone.marionette';
import router from '../../router';
import settings from '../../util/settings';
import hotkeys from '../../util/hotkeys';
import template from './TreeView.hbs';
import StatusToggleView from '../status-toggle/StatusToggleView';
import NodeSorterView from '../node-sorter/NodeSorterView';
import getComparator from '../../data/tree/comparator';
import {byStatuses} from '../../data/tree/filter';
import {on, regions} from '../../decorators';
import {behavior} from '../../decorators/index';

@behavior('TooltipBehavior', {position: 'bottom'})
@regions({sorter: '.tree__sorter', filter: '.tree__filter'})
class TreeView extends View {
    template = template;

    initialize({treeState, tabName, baseUrl}) {
        this.treeState = treeState;
        this.baseUrl = baseUrl;
        this.tabName = tabName;
        this.statusesKey = tabName + '.visibleStatuses';
        this.sorterSettingsKey = tabName + '.treeSorting';
        this.listenTo(this.treeState, 'change:uid', (m, uid) => this.restoreState(uid));
        this.listenTo(settings, 'change:' + this.statusesKey, this.render);
        this.listenTo(settings, 'change:' + this.sorterSettingsKey, this.render);
        this.listenTo(settings, 'change:showGroupInfo', this.render);
        this.listenTo(hotkeys, 'key:up', this.onKeyUp, this);
        this.listenTo(hotkeys, 'key:down', this.onKeyDown, this);
    }

    onRender() {
        this.changeSelectedCase();
    }

    restoreState() {
        this.$('[data-uid]').each((i, node) => {
            const el = this.$(node);
            el.toggleClass('node__title_active', el.data('uid') === this.treeState.get('uid'));
            el.toggleClass('node__expanded', (this.treeState.has(el.data('uid'))));
        });
        this.$('.node__title_active').parents('.node').toggleClass('node__expanded', true);
    }

    changeSelectedCase() {
        const {suffix} = this.options;
        const previous = this.treeState.previous('uid');
        if (previous) {
            const el = this.$(`[data-uid='${previous}']`);
            el.toggleClass('node__title_active', false);
        }

        const current = this.treeState.get('uid');
        console.log('CURRENT ' + current)
        if (current) {
            const el = this.$(`[data-uid='${current}']`);
            el.toggleClass('node__title_active', true);
            history.navigate(this.baseUrl + '/' + current + (suffix ? '/' + suffix : ''));
            this.$('.node__title_active').parents('.node').toggleClass('node__expanded', true);
        }
    }

    @on('click .node__title')
    onNodeClick(e) {
        this.$(e.currentTarget).parent().toggleClass('node__expanded');
        const uid = this.$(e.currentTarget).data('uid');
        if (this.treeState.has(uid)) {
            this.treeState.unset(uid);
        } else {
            this.treeState.set(uid, true);
        }
    }

    // @on('click .tree__info')
    // onInfoClick() {
    //     const show = settings.get('showGroupInfo');
    //     settings.save('showGroupInfo', !show);
    // }

    serializeData() {
        const showGroupInfo = settings.get('showGroupInfo');
        return {
            baseUrl: this.baseUrl,
            showGroupInfo: showGroupInfo,
            time: this.collection.time,
            statistic: this.collection.statistic,
            tabName: this.tabName,
            items: this.collection.toJSON(),
            shownCases: 0,
            totalCases: 0,
            filtered: false
        };
    }
}

export default TreeView;
