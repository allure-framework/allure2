import './styles.scss';
import {View} from 'backbone.marionette';
import {regions, className} from '../../decorators';
import template from './AppLayout.hbs';
import SideNav from '../../components/side-nav/SideNavView';

@className('app')
@regions({
    content: '.app__content',
    nav: '.app__nav'
})
class AppLayout extends View {
    template = template;

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
