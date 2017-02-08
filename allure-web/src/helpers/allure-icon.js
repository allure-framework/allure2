import {SafeString} from 'handlebars/runtime';

const icons = {
    flaky: {
        className: 'fa fa-bomb',
        tooltip: 'Test is flaky'
    },
    info: {
        className: 'fa fa-info-circle',
        tooltip: 'Info'
    }
};

export default function (value) {
    const icon = icons[value];
    return icon ? new SafeString(`<span class="${icon.className}" data-tooltip="${icon.tooltip}"></span>`) : '';
}
