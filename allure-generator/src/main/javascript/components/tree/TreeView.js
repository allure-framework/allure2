import './styles.scss';
import {View} from 'backbone.marionette';
import settings from '../../util/settings';
import hotkeys from '../../util/hotkeys';
import template from './TreeView.hbs';
import {on, regions, className} from '../../decorators';
import {behavior} from '../../decorators/index';

@className('tree')
@behavior('TooltipBehavior', {position: 'bottom'})
@regions({
    sorter: '.tree__sorter',
    filter: '.tree__filter'
})
class TreeView extends View {
    template = template;

    initialize({treeState, tabName, baseUrl}) {
        this.treeState = treeState;
        this.baseUrl = baseUrl;
        this.tabName = tabName;
        this.statusesKey = tabName + '.visibleStatuses';
        this.sorterSettingsKey = tabName + '.treeSorting';
        this.listenTo(this.treeState, 'change:treeNode', (_, treeNode) => this.restoreState(treeNode));
        this.listenTo(settings, 'change:' + this.statusesKey, this.render);
        this.listenTo(settings, 'change:' + this.sorterSettingsKey, this.render);
        this.listenTo(settings, 'change:showGroupInfo', this.render);
        this.listenTo(hotkeys, 'key:up', this.onKeyUp, this);
        this.listenTo(hotkeys, 'key:down', this.onKeyDown, this);
    }

    onRender() {
        this.changeSelectedCase();
    }

    restoreState(treeNode) {
        const previous = this.treeState.previous('treeNode');
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

    changeSelectedCase() {
        // const {suffix} = this.options;
        // const previous = this.treeState.previous('testResult');
        // if (previous) {
        //     const el = this.$(`[data-uid='${previous}']`);
        //     el.toggleClass('node__title_active', false);
        // }
        //
        // const testGroup = this.treeState.get('testGroup');
        // const testResult = this.treeState.get('testResult');
        // if (testResult) {
        //     const group = this.$(`[data-uid='${testGroup}']`);
        //     const el = group.find(`[data-uid='${testResult}']`);
        //     el.toggleClass('node__title_active', true);
        //     history.navigate(this.baseUrl + '/' + testResult + (suffix ? '/' + suffix : ''));
        //     this.$('.node__title_active').parents('.node').toggleClass('node__expanded', true);
        // }
    }

    @on('click .node__title')
    onNodeClick(e) {
        this.$(e.currentTarget).parent().toggleClass('node__expanded');
        const testResult = this.$(e.currentTarget).data('uid');
        const testGroup = this.$(e.currentTarget).data('parentUid');
        if (testGroup && testResult) {
            this.treeState.unset('treeNode');
            this.treeState.set('treeNode', {testGroup, testResult});
        }
    }

    // @on('click .tree__info')
    // onInfoClick() {
    //     const show = settings.get('showGroupInfo');
    //     settings.save('showGroupInfo', !show);
    // }

    templateContext() {
        return {
            cls: this.className,
            baseUrl: this.baseUrl,
            showGroupInfo: settings.get('showGroupInfo'),
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
