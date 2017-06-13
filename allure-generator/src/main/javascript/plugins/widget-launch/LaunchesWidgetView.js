import {View} from 'backbone.marionette';
import template from './LaunchesWidgetView.hbs';

class LaunchesWidgetView extends View {
    template = template;

    initialize() {
        this.model = this.model.getWidgetData('launches');
    }
}

export default LaunchesWidgetView;