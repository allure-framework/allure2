import {SafeString} from 'handlebars/runtime';

const icons = {
    flaky: {
        className: 'fa fa-bomb',
        tooltip: 'Test is flaky'
    }
};

export default function (value) {
    const icon = icons[value];
    return icon ? new SafeString(`<span class="${icon.className}" data-tooltip="${icon.tooltip}"></span>`) : '';
}
