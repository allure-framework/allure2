import './styles.css';
import {on, className} from '../../decorators';
import settings from '../../util/settings';
import template from './NodeSorterView.hbs';
import {View} from 'backbone.marionette';
import nodeComparator from './NodeComparator';


@className('sorter')
class NodeSorterView extends View {
    template = template;
    sorters = ['sorter.name', 'sorter.duration', 'sorter.status'];

    initialize({sorterSettingsKey}) {
        this.sorterSettingsKey = sorterSettingsKey;
    }

    getSorter(){
        const sortSettings = settings.getTreeSorting(this.sorterSettingsKey);
        const direction =  sortSettings.ascending ? 1 : -1;
        const sorter_name = this.sorters[sortSettings.sorter];
        const sorter = nodeComparator(sorter_name, direction);
        return (a, b) => sorter(a, b);
    }

    @on('click .sorter__item')
    onChangeSorting(e){
        const el = this.$(e.currentTarget);

        settings.save(this.sorterSettingsKey, {
            sorter: el.data('index'),
            ascending: !el.data('asc')
        });

        const ascending = el.data('asc');
        this.$('.sorter_enabled').toggleClass('sorter_enabled');
        el.data('asc', !ascending);
        el.find('.sorter__name').toggleClass('sorter_enabled');
        el.find(ascending? '.fa-sort-asc' : '.fa-sort-desc').toggleClass('sorter_enabled');
    }

    serializeData() {
        const sortSettings = settings.getTreeSorting(this.sorterSettingsKey);
        return {
            sorter: this.sorters.map((sorter, index) => ({
                index: index,
                name : sorter,
                asc: (sortSettings.sorter === index) && sortSettings.ascending,
                desc: (sortSettings.sorter === index) && !sortSettings.ascending
            }))
        };
    }
}

export default NodeSorterView;