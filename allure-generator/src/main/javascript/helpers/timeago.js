import moment from 'moment';
import runtime from 'injectify/runtime';

export default function (date) {
    const threshold = 24 * 3600 * 1000;
    const dateString = Date.now() - date < threshold ? moment(date).fromNow() : moment(date).format('DD MMMM YYYY');
    return new runtime.SafeString(`<span title="${moment(date).format('DD MMMM YYYY, H:mm:ss')}">${dateString}</span>`);
}