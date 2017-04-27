import {View} from 'backbone.marionette';
import {on} from '../../../decorators';
import template from './EnvironmentWidget.hbs';

export default class EnvironmentWidget extends View {
    template = template;

    initialize() {
        this.listLimit = 10;
    }

    @on('click .environment-widget__expand')
    onExpandClick() {
        this.listLimit = this.model.get('environmentItems').length;
        this.render();
    }

    serializeData() {
        var items = this.model.get('environmentItems');
        return {
            items: items.slice(0, this.listLimit),
            overLimit: items.length > this.listLimit
        };
    }
}
