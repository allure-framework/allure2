import * as React from "react";
import * as bem from "b_";
import * as underscore from "underscore.string";

const b = bem.with("DateTime");

const Unix2Date: React.SFC<{ value?: number }> = ({value}) => {
    if (!value) {
        return (
            <span className={b()}>Unknown</span>
        );
    }

    const date = new Date(value);
    const dateString = [
        underscore.pad(date.getDate().toString(), 2, '0'),
        underscore.pad((date.getMonth() + 1).toString(), 2, '0'),
        date.getFullYear()
    ].join('/');
    return (
        <span className={b()}>{dateString}</span>
    );
};

const Unix2Time: React.SFC<{ value?: number }> = ({value}) => {
    if (!value) {
        return (
            <span className={b()}>Unknown</span>
        );
    }

    const date = new Date(value);
    const timeString = [
        date.getHours(),
        underscore.pad(date.getMinutes().toString(), 2, '0'),
        underscore.pad(date.getSeconds().toString(), 2, '0')
    ].join(':');

    return (
        <span className={b()}>{timeString}</span>
    );
};

export {
    Unix2Date, Unix2Time
};