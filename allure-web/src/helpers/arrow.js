import {SafeString} from 'handlebars/runtime';
import {values} from '../util/statuses';


export default function(item) {
    var status = values.slice().reverse().reduce((result, current) => {
        return item && (item[current] || item.status === current) ? current : result;
    });

    return new SafeString(`<span class="fa fa-chevron-right fa-fw text_status_${status}"></span>`);
}