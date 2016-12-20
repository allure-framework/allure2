import pad from 'underscore.string/pad';

export default function (date) {
    if(!(date instanceof Date)) {
        date = new Date(date);
    }
    return [pad(date.getDate(), 2, '0'), pad(date.getMonth() + 1, 2, '0'), date.getFullYear()].join('/');
}
