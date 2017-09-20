import './styles.scss';
import {className, on} from '../../decorators';
import template from './NodeSearchView.hbs';
import {View} from 'backbone.marionette';

@className('search')
class NodeSearchView extends View {
    template = template;

    initialize({onSearch, searchQuery}) {
        this.onSearch = onSearch;

        this.searchQuery = searchQuery;
    }

    onRender() {
        this.$('input').val(this.searchQuery);
    }

    @on('input input')
    onChangeSorting(e) {
        this.onSearch(e.target.value);
    }

}

export default NodeSearchView;
