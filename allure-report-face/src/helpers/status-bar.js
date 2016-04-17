import {SafeString} from 'handlebars/runtime';

export default function statusBar(statistic) {
    const fill = ['failed', 'broken', 'canceled', 'pending', 'passed'].map(status =>
        `<div class="bar__fill bar__fill_status_${status.toUpperCase()}"` +
        `style="width: ${statistic[status] / statistic.total * 100}%">${statistic[status]}</div>`
    ).join('');
    return new SafeString(`<div class="bar">${fill}</div>`);
}
