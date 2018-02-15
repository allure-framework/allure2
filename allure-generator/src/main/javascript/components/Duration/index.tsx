import "./styles.scss";
import * as React from "react";
import * as bem from "b_";
import * as underscore from "underscore.string";

interface DateToken {
    suffix: string;
    method: (time: Date) => number;
    pad?: number;
    value?: number;
}

const dateTokens: Array<DateToken> = [
    {
        suffix: 'd',
        method: time => Math.floor(time.valueOf() / (24 * 3600 * 1000))
    },
    {
        suffix: 'h',
        method: time => time.getUTCHours()
    },
    {
        suffix: 'm',
        method: time => time.getUTCMinutes()
    },
    {
        suffix: 's',
        method: time => time.getUTCSeconds()
    },
    {
        pad: 3,
        suffix: 'ms',
        method: time => time.getUTCMilliseconds()
    }
];

const b = bem.with("Duration");

const Duration: React.SFC<{ value?: number, count?: number }> = ({value, count}) => {
    if (value === 0) {
        return <span className={b()}>0s</span>
    }
    if (!value) {
        return <span className={b()}>Unknown</span>
    }
    const time = new Date(value);
    const result = dateTokens
        .map(({method, suffix, pad}) => ({
            value: method(time), suffix, pad, method
        }))
        .reduce(({hasValue, out}, token) => {
            hasValue = hasValue || token.value > 0;
            if (token.value > 0 || (hasValue && token.suffix !== 'ms')) {
                out.push(token);
            }
            return {hasValue, out};
        }, {hasValue: false, out: [] as DateToken[]})
        .out
        .map((token, index) => {
            const value = index === 0 ? token.value : underscore.pad((token.value || 0).toString(), token.pad || 2, '0');
            return value + token.suffix;
        });

    return <span className={b()}>{result.slice(0, count || 2).join(' ')}</span>
};

export default Duration;