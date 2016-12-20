import './styles.css';
import {View} from 'backbone.marionette';
import {region, className} from '../../decorators';
import template from './AppLayout.hbs';
import SideNav from '../../components/side-nav/SideNavView';

@className('app')
class AppLayout extends View {
    template = template;

    @region('.app__content')
    content;
    @region('.app__nav')
    nav;

    initialize() {}

    onRender() {
        this.showChildView('nav', new SideNav());
        const dataPromise = this.loadData();
        if(dataPromise) {
            dataPromise.then(() => {
                this.showChildView('content', this.getContentView());
                this.onViewReady();
            });
        } else {
            this.showChildView('content', this.getContentView());
        }
    }

    onViewReady() {}

    loadData() {}

    getContentView() {
        throw new Error('attempt to call abstract method');
    }
}

export default AppLayout;
