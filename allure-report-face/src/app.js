import 'font-awesome/css/font-awesome.css';
import './styles.css';
import './blocks/table/styles.css';

import {Application, Behaviors} from 'backbone.marionette';
import router from './router';
import * as behaviors from './behaviors';
import ErrorLayout from './layouts/error/ErrorLayout';
import i18next, { init as initTranslations } from './util/translation';

function rootPath(path) {
    return path.split('/')[0];
}

Behaviors.behaviorsLookup = behaviors;

class App extends Application {
    constructor() {
        super({
            regions: {
                'error': '#alert',
                'content': '#content',
                'popup': '#popup'
            }
        });
        this.on('start', () => {
            initTranslations();
            router.on('route:notFound', this.showView(this.tabNotFound));
            i18next.on('languageChanged', () => {
                this.getRegion('content').reset();
                router.reload();
            });
        });
    }

    showView(factory) {
        return (...args) => {
            const view = this.getRegion('content').currentView;
            if(view && rootPath(router.getCurrentUrl()) === rootPath(router.lastUrl)) {
                view.onRouteUpdate(...args);
            } else {
                this.getRegion('content').show(factory(...args));
            }
        };
    }

    tabNotFound() {
        return new ErrorLayout({
            code: 404,
            message: 'Not Found'
        });
    }
}

export default new App();
