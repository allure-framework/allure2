import {SafeString} from 'handlebars/runtime';

export default function (value) {
    switch (value) {
        case 'flaky':
            return new SafeString('<span class="fa fa-bomb" data-tooltip="Flaky"></span>');
        default:
            return '';
    }
}
