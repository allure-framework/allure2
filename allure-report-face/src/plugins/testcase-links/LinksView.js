import './styles.css';
import {ItemView} from 'backbone.marionette';
import {className} from '../../decorators';
import template from './LinksView.hbs';

@className('pane__section')
class LinksView extends ItemView {
    template = template;

    serializeData() {
        return {
            links: this.model.links
        };
    }
}

export default LinksView;
