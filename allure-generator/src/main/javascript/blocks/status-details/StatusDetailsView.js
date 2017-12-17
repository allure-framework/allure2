import './styles.scss';
import {View} from 'backbone.marionette';
import {className, events, modelEvents, ui} from '../../decorators/index';
import template from './StatusDetailsView.hbs';

@className('status-details')
@ui({
    toggle: '.status-details__trace-toggle'
})
@events({
    'click @ui.toggle': 'onStacktraceClick'
})
@modelEvents({
    'change:expanded': 'render'
})
class StatusDetailsView extends View {
    template = template;

    onStacktraceClick() {
        const expanded = this.model.get('expanded');
        this.model.set('expanded', !expanded);
    }

    templateContext() {
        return {
            cls: this.className
        };
    }

}

export default StatusDetailsView;