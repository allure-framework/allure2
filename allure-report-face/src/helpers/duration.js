import pad from 'underscore.string/pad';
import {isFunction} from 'underscore';

const dateTokens = [
    {
        suffix: 'd',
        method: time => Math.floor(time.valueOf() / (24 * 3600 * 1000))
    },
    {
        suffix: 'h',
        method: 'getUTCHours'
    },
    {
        suffix: 'm',
        method: 'getUTCMinutes'
    },
    {
        suffix: 's',
        method: 'getUTCSeconds'
    },
    {
        pad: 3,
        suffix: 'ms',
        method: 'getUTCMilliseconds'
    }
];

export default function(timeInt, count) {
    if(!timeInt) {
        return '0s';
    }
    const time = new Date(timeInt);
    const res = dateTokens.map(({method, suffix, pad}) => ({
            value: isFunction(method) ? method(time) : time[method](),
            suffix, pad
        }))
        .reduce(({hasValue, out}, token) => {
            hasValue = hasValue || token.value > 0;
            if(hasValue) {
                out.push(token);
            }
            return {hasValue, out};
        }, {hasValue: false, out: []})
        .out
        .map(function(token, index) {
            const value = index === 0 ? token.value : pad(token.value, token.pad || 2, '0');
            return value + token.suffix;
        });
    if(typeof count !== 'number') {
        count = 3;
    }
    return res.slice(0, count).join(' ');
}
