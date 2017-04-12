import AppLayout from '../../layouts/application/AppLayout';
import TimelineModel from './TimelineModel';
import TimelineView from './TimelineView';

export default class TimelineLayout extends AppLayout {

    initialize() {
        super.initialize();
        this.model = new TimelineModel();
    }

    loadData() {
        return this.model.fetch();
    }

    getContentView() {
        return new TimelineView({model: this.model});
    }
}
