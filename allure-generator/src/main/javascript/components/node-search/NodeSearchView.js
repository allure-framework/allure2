import './styles.scss';
import {className, on} from '../../decorators';
import template from './NodeSearchView.hbs';
import {View} from 'backbone.marionette';

@className('search')
class NodeSearchView extends View {
    template = template;

    initialize({onSearch}) {
        this.onSearch = onSearch;
    }

    @on('input input')
    onChangeSorting(e) {
        this.onSearch(e.target.value);
    }

}

export default NodeSearchView;
