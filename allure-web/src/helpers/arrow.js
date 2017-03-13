import {SafeString} from 'handlebars/runtime';
import {values} from '../util/statuses';


export default function(item) {
    var status = (item && item.status) ? item.status : 'unknown';
    return new SafeString(`<span class="fa fa-chevron-right fa-fw text_status_${status}"></span>`);
}