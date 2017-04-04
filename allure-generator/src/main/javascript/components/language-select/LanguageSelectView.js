import './styles.css';
import PopoverView from '../popover/PopoverView';
import {className, on} from '../../decorators';
import template from './LanguageSelectView.hbs';
import i18next, { LANGUAGES } from '../../util/translation';
import settings from '../../util/settings';
import $ from 'jquery';

@className('language-select popover')
class LanguageSelectView extends PopoverView {

    initialize() {
        super.initialize({position: 'right'});
    }

    setContent() {
        this.$el.html(template({
            languages: LANGUAGES,
            currentLang: settings.get('language')
        }));
    }

    show(anchor) {
        super.show(null, anchor);
        this.delegateEvents();
        setTimeout(() => {
            $(document).one('click', () => this.hide());
        });
    }

    @on('click .language-select__item')
    onLanguageClick(e) {
        const langId = this.$(e.currentTarget).data('id');
        settings.save('language', langId);
        i18next.changeLanguage(langId);
    }

}

export default LanguageSelectView;
