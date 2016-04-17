import pad from 'underscore.string/pad';

export default function (date) {
    if(!(date instanceof Date)) {
        date = new Date(date);
    }
    return [pad(date.getDay(), 2, '0'), pad(date.getMonth(), 2, '0'), date.getFullYear()].join('/');
}
