import i18next from '../util/translation';

export default function translate(key, options) {
    return i18next.t(key, options.hash);
}
