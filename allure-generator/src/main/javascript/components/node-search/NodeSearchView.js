import './styles.scss';
import {className, on} from '../../decorators';
import template from './NodeSearchView.hbs';
import {View} from 'backbone.marionette';

@className('search')
class NodeSearchView extends View {
    template = template;

    initialize({settings}) {
        this.settings = settings;
    }

    onRender() {
        this.$('input').val(this.settings.getSearchQuery());
    }

    @on('input input')
    onChangeSorting(e) {
        this.settings.setSearchQuery(e.target.value);
    }

    close() {
        this.settings.setSearchQuery('');
    }

}

export default NodeSearchView;
