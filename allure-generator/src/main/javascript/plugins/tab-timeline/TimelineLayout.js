import AppLayout from '../../layouts/application/AppLayout';
import TimelineView from './TimelineView';
import TreeCollection from '../../data/tree/TreeCollection';

export default class TimelineLayout extends AppLayout {

    initialize({url}) {
        super.initialize();
        this.items = new TreeCollection([], {url});
    }

    loadData() {
        return this.items.fetch();
    }

    getContentView() {
        return new TimelineView({collection: this.items});
    }

}
