import './styles.scss';
import {View} from 'backbone.marionette';
import template from './TreeViewContainer.hbs';
import {behavior, className, on, regions} from '../../decorators';
import getComparator from '../../data/tree/comparator';
import {byStatuses, byText, mix} from '../../data/tree/filter';
import NodeSorterView from '../node-sorter/NodeSorterView';
import NodeSearchView from '../node-search/NodeSearchView';
import StatusToggleView from '../status-toggle/StatusToggleView';
import TreeView from '../tree/TreeView';
import {Model} from 'backbone';
import {getSettingsForTreePlugin} from '../../utils/settingsFactory';

@className('tree')
@behavior('TooltipBehavior', {position: 'bottom'})
@regions({
    search: '.pane__search',
    sorter: '.tree__sorter',
    filter: '.tree__filter',
    content: '.tree__content'
})
class TreeViewContainer extends View {
    template = template;

    initialize({routeState, tabName, baseUrl, settings = getSettingsForTreePlugin(baseUrl)}) {
        this.state = new Model();
        this.routeState = routeState;
        this.baseUrl = baseUrl;
        this.tabName = tabName;
        this.listenTo(this.routeState, 'change:testResultTab', this.render);

        this.settings = settings;
        this.listenTo(this.settings, 'change', this.render);
    }


    applyFilters() {
        const visibleStatuses = this.settings.getVisibleStatuses();
        const filter = mix(byText(this.searchQuery), byStatuses(visibleStatuses));

        const sortSettings = this.settings.getTreeSorting();
        const sorter = getComparator(sortSettings);

        this.collection.applyFilterAndSorting(filter, sorter);
    }

    onBeforeRender() {
       this.applyFilters();
    }

    @on('click .tree__info')
    onInfoClick() {
        const show = this.settings.isShowGroupInfo();
        this.settings.setShowGroupInfo(!show);
    }

    onRender() {
        this.renderContent();
        this.showChildView('search', new NodeSearchView({
            onSearch: this.onSearch.bind(this),
            searchQuery: this.searchQuery
        }));
        this.showChildView('sorter', new NodeSorterView({
            settings: this.settings
        }));
        this.showChildView('filter', new StatusToggleView({
            settings: this.settings,
            statistic: this.collection.statistic
        }));
    }

    renderContent() {
        this.showChildView('content', new TreeView({
            state: this.state,
            routeState: this.routeState,
            searchQuery: this.searchQuery,
            tabName: this.tabName,
            baseUrl: this.baseUrl,
            settings: this.settings,
            collection: this.collection
        }));
    }

    onSearch(searchQuery) {
        this.searchQuery = searchQuery;
        this.applyFilters();
        this.renderContent();
    }

    templateContext() {
        return {
            cls: this.className,
            showGroupInfo: this.settings.isShowGroupInfo(),
            tabName: this.tabName,
            shownCases: 0,
            totalCases: 0,
            filtered: false
        };
    }
}

export default TreeViewContainer;
