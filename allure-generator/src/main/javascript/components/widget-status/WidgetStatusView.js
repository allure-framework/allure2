import {View} from 'backbone.marionette';
import template from './WidgetStatusView.hbs';

export default class WidgetStatusView extends View {
    template = template;

    initialize({name, model}) {
        this.model = model.getWidgetData(name);
    }

    serializeData() {
        const showLinks = typeof this.showLinks !== 'undefined' ? this.showLinks : true;
        return Object.assign(super.serializeData(), {
            rowTag: showLinks ? 'a' : 'span',
            title: this.title,
            showAllText: this.showAllText,
            baseUrl: this.baseUrl
        });
    }
}
