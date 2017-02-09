import {SafeString} from 'handlebars/runtime';

const icons = {
    flaky: {
        className: 'fa fa-bomb',
        tooltip: 'Test is flaky'
    },
    failed: {
        className: 'fa fa-times-circle text_status_failed',
        tooltip: 'Failed'
    },
    broken: {
        className: 'fa fa-exclamation-circle text_status_broken',
        tooltip: 'Broken'
    },
    passed: {
        className: 'fa fa-check-circle text_status_passed',
        tooltip: 'Passed'
    },
    skipped: {
        className: 'fa fa-minus-circle text_status_skipped',
        tooltip: 'Skipped'
    },
    unknown: {
        className: 'fa fa-question-circle text_status_unknown',
        tooltip: 'Unknown'
    }
};

export default function (value, extraClasses='') {
    const icon = icons[value];
    return icon ? new SafeString(`<span class="${icon.className} ${extraClasses}" data-tooltip="${icon.tooltip}"></span>`) : '';
}
