import 'font-awesome/css/font-awesome.css';
import './styles.scss';
import {Application, Behaviors} from 'backbone.marionette';
import {history} from 'backbone';
import router from './router';
import * as behaviors from './behaviors';
import ErrorLayout from './layouts/error/ErrorLayout';
import TestResultLayout from './layouts/testresult/TestResultLayout';
import i18next, { initTranslations } from './utils/translation';
import translate from './helpers/t';

//https://github.com/d3/d3-timer/pull/21
if(typeof window.requestAnimationFrame === 'function') {
    window.requestAnimationFrame = window.requestAnimationFrame.bind(window);
}

Behaviors.behaviorsLookup = behaviors;

function rootPath(path) {
    return path.split('/')[0];
}

function noTabChange() {
    return rootPath(router.getCurrentUrl()) === rootPath(router.currentUrl);
}

export function showView(factory) {
    return (...args) => {
        const current = App.getView();
        if (current && noTabChange() && current.shouldKeepState(...args)) {
            current.onRouteUpdate(...args);
        } else {
            App.showView(factory(...args));
        }
    };
}

export function notFound() {
    return new ErrorLayout({code: 404, message: translate('errors.notFound')});
}

const App = new Application({
    region: '#content'
});

App.on('start', () => {
    initTranslations().then(() => {
        history.start();
        document.dir = i18next.dir();
        i18next.on('languageChanged', () => {
            App.getRegion().reset();
            router.reload();
            document.dir = i18next.dir();
        });
    });

    router.on('route:notFound', showView(notFound));
    router.on('route:testresultPage', showView((uid, tabName) => new TestResultLayout({uid, tabName})));
});

export default App;
