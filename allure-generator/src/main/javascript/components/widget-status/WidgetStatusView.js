import {View} from 'backbone.marionette';
import template from './WidgetStatusView.hbs';

export default class WidgetStatusView extends View {
    template = template;
    showLinks = true;

    serializeData() {
        return Object.assign(super.serializeData(), {
            rowTag: this.showLinks ? 'a' : 'span',
            title: this.title,
            showAllText: this.showAllText,
            baseUrl: this.baseUrl
        });
    }
}
