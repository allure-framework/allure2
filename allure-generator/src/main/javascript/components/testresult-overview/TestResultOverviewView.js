import './styles.css';
import {View} from 'backbone.marionette';
import {className, on} from '../../decorators/index';
import template from './TestResultOverviewView.hbs';

@className('test-result-overview')
class TestResultOverviewView extends View {
    template = template;

    templateContext() {
        return {
            cls: this.className
        };
    }

    @on('click .status-details__trace-toggle')
    onStacktraceClick() {
        this.$('.status-details__trace').toggleClass('status-details__trace_expanded');
    }
}

export default TestResultOverviewView;