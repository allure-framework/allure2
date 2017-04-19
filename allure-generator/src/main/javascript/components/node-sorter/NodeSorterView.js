import './styles.css';
import {on, className} from '../../decorators';
import settings from '../../util/settings';
import template from './NodeSorterView.hbs';
import {View} from 'backbone.marionette';
import {values} from '../../util/statuses';


@className('sorter')
class NodeSorterView extends View {
    template = template;

    sorters = [
        {
            key: 'name',
            sorter: (a, b) => {return a.name.toLowerCase() < b.name.toLowerCase() ? -1 : 1;}
        },
        {
            key: 'duration',
            sorter: (a, b) => {return a.time.duration < b.time.duration ? -1 : 1;}
        },
        {
            key: 'status',
            sorter: (a, b) => {
                if ('status' in a && 'status' in b){
                    return values.indexOf(a.status) > values.indexOf(b.status) ? -1 : 1;
                } else if ('statistic' in a && 'statistic' in b){
                    return values.reduce((all, current) => {
                        if ((a.statistic[current] !== b.statistic[current]) && all === 0) {
                            return b.statistic[current] > a.statistic[current];
                        } else {
                            return all;
                        }
                    }, 0) ? -1: 1;
                } else {
                    return 1;
                }
            }
        },
    ];

    initialize({sorterSettingsKey}) {
        this.sorterSettingsKey = sorterSettingsKey;
    }

    getSorter(){
        const sortSettings = settings.getTreeSorting(this.sorterSettingsKey);
        const sorter = this.sorters[sortSettings.sorter].sorter;
        const direction =  sortSettings.ascending ? 1 : -1;
        return (a, b) => sorter(a, b) * direction;
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
                name : sorter.key,
                asc: (sortSettings.sorter === index) && sortSettings.ascending,
                desc: (sortSettings.sorter === index) && !sortSettings.ascending
            }))
        };
    }
}

export default NodeSorterView;