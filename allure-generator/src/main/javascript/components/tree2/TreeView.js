import './styles.scss';
import {CollectionView, View} from 'backbone.marionette';
import {className, modelEvents, regions, tagName, triggers, ui} from '../../decorators';
import {Collection} from 'backbone';
import groupTemplate from './GroupView.hbs';
import leafTemplate from './LeafView.hbs';
import router from '../../router';
import {values} from '../../utils/statuses';

function calculateStatistic(items) {
    const statistic = {};
    values.forEach(value => {
        statistic[value] = 0;
    });
    items.forEach(item => {
        if (item.children) {
            const childStatistic = calculateStatistic(item.children);
            values.forEach(value => {
                statistic[value] += childStatistic[value];
            });
        } else {
            statistic[item.status]++;
        }
    });
    return statistic;
}

@ui({
    row: '.tree-node__row',
    children: '.tree-node__children'
})
@regions({
    children: '@ui.children'
})
@triggers({
    'click @ui.row': 'row:click'
})
@modelEvents({
    'change:expanded': 'render',
    'change:selected': 'render',
})
@tagName('li')
@className('tree-node')
class GroupView extends View {
    template = groupTemplate;

    onRender() {
        const {baseUrl, selectedGroup, selectedLeaf} = this.options;

        if (selectedGroup && !selectedLeaf && selectedGroup === this.model.get('uid')) {
            this.model.set('selected', true);
            this.model.set('expanded', true);
        }

        const expanded = this.model.get('expanded');
        if (expanded) {
            const children = new Collection(this.model.get('children'));
            this.showChildView('children', new TreeView({
                collection: children,
                baseUrl,
                selectedGroup,
                selectedLeaf
            }));
        }
    }

    onRowClick() {
        const expanded = this.model.get('expanded');
        this.model.set('expanded', !expanded);
    }

    templateContext() {
        return {
            cls: this.className,
            statistic: calculateStatistic(this.model.get('children'))
        };
    }
}

@ui({
    row: '.tree-node__row'
})
@triggers({
    'click @ui.row': 'row:click'
})
@modelEvents({
    'change:selected': 'render'
})
@tagName('li')
@className('tree-node')
class LeafView extends View {
    template = leafTemplate;

    onRender() {
        const {selectedGroup, selectedLeaf} = this.options;
        const matchGroup = selectedGroup ? selectedGroup === this.model.get('parentUid') : true;
        const matchLeaf = selectedLeaf && selectedLeaf === this.model.get('uid');

        if (matchGroup && matchLeaf) {
            this.model.set('selected', true);
        }
    }

    onRowClick() {
        const {baseUrl} = this.options;
        router.toUrl(`${baseUrl}/${this.model.get('parentUid')}/${this.model.get('uid')}`);
    }

    templateContext() {
        const {baseUrl} = this.options;
        return {
            cls: this.className,
            baseUrl
        };
    }
}

@tagName('ul')
@className('tree')
class TreeView extends CollectionView {

    childView(child) {
        return child.has('children') ? GroupView : LeafView;
    }

    childViewOptions() {
        const {baseUrl, selectedGroup, selectedLeaf} = this.options;
        return {
            baseUrl,
            selectedGroup,
            selectedLeaf
        };
    }
}

export default TreeView;