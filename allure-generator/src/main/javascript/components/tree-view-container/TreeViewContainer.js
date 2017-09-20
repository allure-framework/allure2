import './styles.scss';
import {View} from 'backbone.marionette';
import template from './TreeView.hbs';
import {behavior, className, regions} from '../../decorators';
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
    search: '.tree__search',
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
            routeState: this.routeState,
            searchQuery: this.searchQuery,
            tabName: this.tabName,
            baseUrl: this.baseUrl,
            settings: this.settings,
            items: this.collection.toJSON(),
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
            baseUrl: this.baseUrl,
            showGroupInfo: this.settings.isShowGroupInfo(),
            time: this.collection.time,
            statistic: this.collection.statistic,
            uid: this.collection.uid,
            tabName: this.tabName,
            items: this.collection.toJSON(),
            testResultTab: this.routeState.get('testResultTab') || '',
            shownCases: 0,
            totalCases: 0,
            filtered: false
        };
    }
}

export default TreeViewContainer;
