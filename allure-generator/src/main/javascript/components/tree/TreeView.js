import './styles.scss';
import {View} from 'backbone.marionette';
import hotkeys from '../../utils/hotkeys';
import template from './TreeView.hbs';
import {behavior, className, on} from '../../decorators';
import router from '../../router';
import {Model} from 'backbone';
import {getSettingsForTreePlugin} from '../../utils/settingsFactory';

@className('tree')
@behavior('TooltipBehavior', {position: 'bottom'})
class TreeView extends View {
    template = template;

    initialize({routeState, searchQuery, tabName, baseUrl, settings = getSettingsForTreePlugin(baseUrl)}) {
        this.state = new Model();
        this.routeState = routeState;
        this.baseUrl = baseUrl;
        this.tabName = tabName;
        this.searchQuery = searchQuery;
        this.setState();
        this.listenTo(this.routeState, 'change:treeNode', this.selectNode);
        this.listenTo(this.routeState, 'change:testResultTab', this.render);

        this.settings = settings;

        this.listenTo(hotkeys, 'key:up', this.onKeyUp, this);
        this.listenTo(hotkeys, 'key:down', this.onKeyDown, this);
        this.listenTo(hotkeys, 'key:esc', this.onKeyBack, this);
        this.listenTo(hotkeys, 'key:left', this.onKeyBack, this);
    }

    setState() {
        const treeNode = this.routeState.get('treeNode');
        if (treeNode && treeNode.testResult) {
            const uid = treeNode.testResult;
            this.state.set(uid, true);
        }
        if (treeNode && treeNode.testGroup) {
            const uid = treeNode.testGroup;
            this.state.set(uid, true);
        }
    }


    onRender() {
        this.selectNode();
        if (this.searchQuery) {
            this.$('.node__title').each((i, node) => this.expandNode(this.$(node), true));
        } else {
            this.restoreState();
        }
    }

    selectNode() {
        const previous = this.routeState.previous('treeNode');
        this.toggleNode(previous, false);
        const current = this.routeState.get('treeNode');
        this.toggleNode(current, true);
        this.restoreState();
    }

    toggleNode(node, active = true) {
        if (node) {
            const el = this.findElement(node);
            el.toggleClass('node__title_active', active);
            this.changeState(node.testResult);
            this.changeState(node.testGroup);
        }
    }

    changeState(uid, active = true) {
        if (active) {
            this.state.set(uid, true);
        } else {
            this.state.unset(uid);
        }
    }

    restoreState() {
        this.$('[data-uid]').each((i, node) => {
            const el = this.$(node);
            const uid = el.data('uid');
            el.toggleClass('node__expanded', this.state.has(uid));
        });
        this.$('.node__title_active').parents('.node').toggleClass('node__expanded', true);
        this.$('.node__expanded').parents('.node').toggleClass('node__expanded', true);
    }

    findElement(treeNode) {
        if (treeNode.testResult) {
            return this.$(`[data-uid='${treeNode.testResult}'][data-parentUid='${treeNode.testGroup}']`);
        } else {
            return this.$(`[data-uid='${treeNode.testGroup}']`);
        }
    }

    expandNode(node, keepState) {
        const uid = node.data('uid');
        if (!keepState) {
            this.changeState(uid, !this.state.has(uid));
        }
        this.$(node).parent().toggleClass('node__expanded');
    }

    @on('click .node__title')
    onNodeClick(e) {
        this.expandNode(this.$(e.currentTarget));
    }

    @on('click .tree__info')
    onInfoClick() {
        const show = this.settings.isShowGroupInfo();
        this.settings.setShowGroupInfo(!show);
    }

    onKeyUp(event) {
        event.preventDefault();
        const current = this.routeState.get('treeNode');
        if (current && current.testResult) {
            this.selectTestResult(this.collection.getPreviousTestResult(current.testResult));
        } else {
            this.selectTestResult(this.collection.getLastTestResult());
        }
    }

    onKeyDown(event) {
        event.preventDefault();
        const current = this.routeState.get('treeNode');
        if (current && current.testResult) {
            this.selectTestResult(this.collection.getNextTestResult(current.testResult));
        } else {
            this.selectTestResult(this.collection.getFirstTestResult());
        }
    }

    onKeyBack(event) {
        event.preventDefault();
        const current = this.routeState.get('treeNode');
        if (!current) {
            return;
        }
        if (current.testGroup && current.testResult) {
            router.toUrl(`${this.baseUrl}/${current.testGroup}`);
        } else if (current.testGroup) {
            router.toUrl(`${this.baseUrl}`);
        }
    }

    selectTestResult(testResult) {
        if (testResult) {
            const tab = this.routeState.get('testResultTab') || '';
            router.toUrl(`${this.baseUrl}/${testResult.parentUid}/${testResult.uid}/${tab}`, {replace: true});
        }
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

export default TreeView;
