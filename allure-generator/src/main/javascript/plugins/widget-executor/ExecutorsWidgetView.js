import {View} from 'backbone.marionette';
import template from './ExecutorsWidgetView.hbs';

class ExecutorsWidgetView extends View {
    template = template;

    initialize() {
        this.model = this.model.getWidgetData('executors');
    }
}

export default ExecutorsWidgetView;