import {SafeString} from 'handlebars/runtime';
import {values} from '../utils/statuses';

export default function statusBar(statistic) {
    const fill = values.map(status => {
        const count = typeof statistic[status] === 'undefined' ? 0 : statistic[status];
        const percents = count / statistic.total * 100;
        return count === 0
            ? ''
            : `<div class="bar__fill bar__fill_status_${status}" style="width: ${percents}%">${count}</div>`;
    }
    ).join('');
    return new SafeString(`<div class="bar">${fill}</div>`);
}
