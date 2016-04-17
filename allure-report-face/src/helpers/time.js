import pad from 'underscore.string/pad';

export default function (date) {
    if(!(date instanceof Date)) {
        date = new Date(date);
    }
    return [date.getHours(), pad(date.getMinutes(), 2, '0'), pad(date.getSeconds(), 2, '0')].join(':');
}
