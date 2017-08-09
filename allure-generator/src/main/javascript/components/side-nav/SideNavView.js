import './styles.scss';
import {className, on} from '../../decorators';
import {findWhere} from 'underscore';
import {View} from 'backbone.marionette';
import TooltipView from '../tooltip/TooltipView';
import LanguageSelectView from '../language-select/LanguageSelectView';
import { LANGUAGES } from '../../utils/translation';
import pluginsRegistry from '../../utils/pluginsRegistry';
import template from './SideNavView.hbs';
import {escapeExpression as escape} from 'handlebars/runtime';
import router from '../../router';
import settings from '../../utils/settings';

@className('side-nav')
class SideNavView extends View {
    template = template;

    initialize() {
        this.tabs = pluginsRegistry.tabs.map(({tabName, icon, title}) => ({
            tabName, icon, title,
            active: this.isTabActive(tabName)
        }));
        this.tooltip = new TooltipView({position: 'right'});
        this.langSelect = new LanguageSelectView();
    }

    onRender() {
        this.$el.toggleClass('side-nav_collapsed', settings.isSidebarCollapsed());
    }

    onDestroy() {
        this.tooltip.hide();
    }

    serializeData() {
        return {
            language: findWhere(LANGUAGES, {id: settings.getLanguage()}),
            tabs: this.tabs
        };
    }

    isTabActive(name) {
        const currentUrl = router.getCurrentUrl();
        return name ? currentUrl.indexOf(name) === 0 : currentUrl === name;
    }

    @on('mouseenter [data-tooltip]')
    onSidelinkHover(e) {
        if(this.$el.hasClass('side-nav_collapsed')) {
            const el = this.$(e.currentTarget);
            this.tooltip.show(escape(el.data('tooltip')), el);
        }
    }

    @on('mouseleave [data-tooltip]')
    onSidelinkLeave() {
        this.tooltip.hide();
    }

    @on('click .side-nav__collapse')
    onCollapseClick() {
        this.$el.toggleClass('side-nav_collapsed');
        settings.setSidebarCollapsed( this.$el.hasClass('side-nav_collapsed'));
        this.tooltip.hide();
    }

    @on('click .side-nav__language')
    @on('click .side-nav__language-small')
    onLanguageClick(e) {
        if(this.langSelect.isVisible()) {
            this.langSelect.hide();
        } else {
            this.langSelect.show(this.$(e.currentTarget));
        }
        this.tooltip.hide();
    }
}

export default SideNavView;
