import './styles.css';
import $ from 'jquery';
import {on, className} from '../../decorators';
import settings from '../../util/settings';
import template from './NodeSorterView.hbs';
import {View} from 'backbone.marionette';


@className('sorter')
class NodeSorterView extends View {
    template = template;
    sorters = [
        'name',
        'status',
        'duration'
    ]

    initialize({sorterSettingsKey}) {
        this.sorterSettingsKey = sorterSettingsKey;
        //super.initialize({position: 'bottom-left', offset: -1});
        //this.onDocumentClick = this.onDocumentClick.bind(this);
    }
/*
    onDocumentClick(e) {
        if(!this.$(e.target).length) {
            this.hide();
        }
    }

    setContent() {
        //const sorterSettings = settings.getTreeSorting(this.sorterSettingsKey);
        console.log(this.serializeData())
        //this.$el.html(template(sorterSettings));
    }

    show(anchor) {
        super.show(null, anchor);
        this.delegateEvents();
        setTimeout(() => {
            $(document).on('click', this.onDocumentClick);
        });
    }

    hide() {
        $(document).off('click', this.onDocumentClick);
        super.hide();
    }
*/
    @on('click .sorter__item')
    onChangeSorting(e){
        const el = this.$(e.currentTarget);

        const sortSettings = settings.getTreeSorting(this.sorterSettingsKey);
        sortSettings.sorter = this.sorters.indexOf(el.data('name'));
        sortSettings.ascending = !el.data('asc');
        console.log(this.sorterSettingsKey)
        settings.save(this.sorterSettingsKey, Object.assign({}, sortSettings));

        this.$('.sorter_enabled').toggleClass('sorter_enabled');
        el.data('asc', sortSettings.ascending);
        el.find('.sorter__name').toggleClass('sorter_enabled');
        el.find(sortSettings.ascending ? '.fa-sort-asc' : '.fa-sort-desc').toggleClass('sorter_enabled');
    }

    serializeData() {
        const sortSettings = settings.getTreeSorting(this.sorterSettingsKey);
        return {
            sorter: this.sorters.map((sorter, index) => ({
                name : sorter,
                asc: (sortSettings.sorter === index) && sortSettings.ascending,
                desc: (sortSettings.sorter === index) && !sortSettings.ascending
            }))
        };
    }
}

export default NodeSorterView;