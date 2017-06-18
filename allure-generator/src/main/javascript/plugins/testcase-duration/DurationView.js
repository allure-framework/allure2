import {View} from 'backbone.marionette';
import {className, on} from '../../decorators';
import template from './DurationView.hbs';
import clipboard from 'clipboard-js';
import dateHelper from '../../helpers/date';
import durationHelper from '../../helpers/duration';

@className('pane__section')
class DurationView extends View {
    template = template;

    serializeData() {
        return {
            time: this.model.get('time')
        };
    }

    @on('click .testcase-duration')
    onDurationClick() {
        const {start, stop, duration} = this.model.get('time');
        clipboard.copy(`Started: ${dateHelper(start)}, `
            + `finished: ${dateHelper(stop)}, `
            + `duration: ${durationHelper(duration)}`);
    }
}

export default DurationView;
