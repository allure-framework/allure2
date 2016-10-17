import './styles.css';
import {View} from 'backbone.marionette';
import template from './HistoryView.hbs';

class HistoryView extends View {
    template = template;

    serializeData() {
        var extra = this.model.get('extra');
        return {
            history: extra ? extra.history : null
        };
    }
}

export default HistoryView;