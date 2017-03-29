import {Model} from 'backbone';

export default class WidgetsModel extends Model {
    url = 'data/widgets.json';

    getWidgetData(name) {
        const items = this.get(name);
        console.log(name);
        console.log(items);
        return new Model(Array.isArray(items) ? {items} : items);
    }
}
