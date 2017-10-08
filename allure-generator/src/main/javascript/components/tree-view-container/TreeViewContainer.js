import './styles.scss';
import {View} from 'backbone.marionette';
import template from './TreeViewContainer.hbs';
import {behavior, className, on, regions} from '../../decorators';
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

    initialize({routeState, state = new Model(), tabName, baseUrl, settings = getSettingsForTreePlugin(baseUrl)}) {
        this.state = state;
        this.routeState = routeState;
        this.baseUrl = baseUrl;
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
        this.showChildView('content', new TreeView({
            state: this.state,
            routeState: this.routeState,
            tabName: this.tabName,
            baseUrl: this.baseUrl,
            settings: this.settings,
            collection: this.collection
        }));

        this.showChildView('search', new NodeSearchView({
            state: this.state
        }));
        this.showChildView('sorter', new NodeSorterView({
            settings: this.settings
        }));
        this.showChildView('filter', new StatusToggleView({
            settings: this.settings,
            statistic: this.collection.statistic
        }));
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
