import './styles.scss';
import {View} from 'backbone.marionette';
import template from './TreeViewContainer.hbs';
import {behavior, className, events, on, regions, ui} from '../../decorators';
import NodeSorterView from '../node-sorter/NodeSorterView';
import NodeSearchView from '../node-search/NodeSearchView';
import StatusToggleView from '../status-toggle/StatusToggleView';
import TreeView from '../tree2/TreeView';
import {Model} from 'backbone';
import {getSettingsForTreePlugin} from '../../utils/settingsFactory';
import hotkeys from '../../utils/hotkeys';

@className('tree-container')
@behavior('TooltipBehavior', {position: 'bottom'})
@ui({
    content: '.tree-container__content'
})
@regions({
    search: '.pane__search',
    sorter: '.tree-container__sorter',
    filter: '.tree-container__filter',
    content: '@ui.content'
})
@events({
    'focus @ui.content': 'onContentFocus',
    'blur @ui.content': 'onContentBlur'
})
class TreeViewContainer extends View {
    template = template;

    initialize({routeState, state = new Model(), tabName, baseUrl, csvUrl=null, settings = getSettingsForTreePlugin(baseUrl)}) {
        this.state = state;
        this.routeState = routeState;
        this.baseUrl = baseUrl;
        this.csvUrl = csvUrl;
        this.tabName = tabName;
        this.listenTo(this.routeState, 'change:testResultTab', this.render);
        this.settings = settings;
    }


    @on('click .tree__info')
    onInfoClick() {
        const show = this.settings.isShowGroupInfo();
        this.settings.setShowGroupInfo(!show);
    }

    onRender() {
        const treeNode = this.routeState.get('treeNode');
        const testGroup = treeNode ? treeNode.testGroup : null;
        const testResult = treeNode ? treeNode.testResult : null;

        console.log('group:', testGroup)
        console.log('result:', testResult)

        this.showChildView('content', new TreeView({
            state: this.state,
            routeState: this.routeState,
            tabName: this.tabName,
            baseUrl: this.baseUrl,
            selectedGroup: testGroup,
            selectedLeaf: testResult,
            settings: this.settings,
            collection: this.collection
        }));

        // this.showChildView('search', new NodeSearchView({
        //     state: this.state
        // }));
        // this.showChildView('sorter', new NodeSorterView({
        //     settings: this.settings
        // }));
        // this.showChildView('filter', new StatusToggleView({
        //     settings: this.settings,
        //     statistic: this.collection.statistic
        // }));
    }

    onContentFocus() {
        this.listenTo(hotkeys, 'key:up', this.onKeyUp, this);
        this.listenTo(hotkeys, 'key:down', this.onKeyDown, this);
    }

    onContentBlur() {
        this.stopListening(hotkeys, 'key:up');
        this.stopListening(hotkeys, 'key:down');
    }

    onKeyUp(e) {
        e.preventDefault();
        console.log('UP');
        this.getChildView('content').triggerMethod('key:up');
    }

    onKeyDown(e) {
        e.preventDefault();
        console.log('DOWN')
        this.getChildView('content').triggerMethod('key:down');
    }



    templateContext() {
        return {
            cls: this.className,
            showGroupInfo: this.settings.isShowGroupInfo(),
            tabName: this.tabName,
            shownCases: 0,
            totalCases: 0,
            filtered: false,
            csvUrl: this.csvUrl
        };
    }
}

export default TreeViewContainer;
