import {ItemView} from 'backbone.marionette';
import {on} from '../../../decorators';
import template from './EnvironmentWidget.hbs';

export default class EnvironmentWidget extends ItemView {
    template = template;

    initialize() {
        this.listLimit = 10;
    }

    @on('click .environment-widget__expand')
    onExpandClick() {
        this.listLimit = this.model.get('items').length;
        this.render();
    }

    serializeData() {
        var items = this.model.get('items');
        return {
            items: items.slice(0, this.listLimit),
            overLimit: items.length > this.listLimit
        };
    }
}
