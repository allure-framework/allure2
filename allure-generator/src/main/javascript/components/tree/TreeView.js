import './styles.css';
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

    initialize({state, tabName, baseUrl}) {
        this.state = state;
        this.baseUrl = baseUrl;
        this.tabName = tabName;
        this.statusesKey = tabName + '.visibleStatuses';
        this.sorterSettingsKey = tabName + '.treeSorting';
        this.listenTo(this.state, 'change:testcase', (m, testcase) => this.restoreState(testcase));
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
        this.restoreState();
        this.showChildView('sorter', new NodeSorterView({sorterSettingsKey: this.sorterSettingsKey}));
        this.showChildView('filter', new StatusToggleView({statusesKey: this.statusesKey, statistic: this.collection.statistic}));
    }

    @on('click .node__title')
    onNodeClick(e) {
        this.$(e.currentTarget).parent().toggleClass('node__expanded');
        const uid = this.$(e.currentTarget).data('uid');
        if (this.state.has(uid)) {
            this.state.unset(uid);
        } else {
            this.state.set(uid, true);
        }
    }

    @on('click .tree__info')
    onInfoClick() {
        const show = settings.get('showGroupInfo');
        settings.save('showGroupInfo', !show);
    }

    onKeyUp(event) {
        event.preventDefault();
        const currentCaseUid = this.state.get('testcase');
        if(currentCaseUid) {
            this.selectTestcase(this.collection.getPreviousTestcase(currentCaseUid));
        }
    }

    onKeyDown(event) {
        event.preventDefault();
        const currentCaseUid = this.state.get('testcase');
        if(currentCaseUid) {
            this.selectTestcase(this.collection.getNextTestcase(currentCaseUid));
        }
    }

    selectTestcase(testcase) {
        if(testcase) {
            router.toUrl(`${this.baseUrl}/${testcase.uid}`);
        }
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
        const showGroupInfo = settings.get('showGroupInfo');
        const shownCases = this.collection.testcases.length;
        const totalCases = this.collection.allTestcases.length;
        return {
            baseUrl: this.baseUrl,
            showGroupInfo: showGroupInfo,
            time: this.collection.time,
            statistic: this.collection.statistic,
            tabName: this.tabName,
            items: this.collection.toJSON(),
            shownCases: shownCases,
            totalCases: totalCases,
            filtered: shownCases !== totalCases
        };
    }
}

export default TreeView;
