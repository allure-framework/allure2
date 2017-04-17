import {View} from 'backbone.marionette';
import template from './WidgetStatusView.hbs';

export default class WidgetStatusView extends View {
    template = template;

    serializeData() {
        return Object.assign(super.serializeData(), {
            title: this.title,
            showAllText: this.showAllText,
            baseUrl: this.baseUrl
        });
    }
}
