import {LayoutView} from 'backbone.marionette';
import {on} from '../../decorators';
import {doSort, updateSort} from '../../util/sorting';
import router from '../../router';
import settings from '../../util/settings';

const KEY_UP = 38;
const KEY_DOWN = 40;

class DataGridView extends LayoutView {
    settingsKey = null;

    getSettings() {
        return settings.get(this.settingsKey) || {field: 'title', order: 'asc'};
    }

    setSettings(sorting) {
        settings.save(this.settingsKey, sorting);
    }

    applySort(data) {
        return doSort(data, this.getSettings());
    }

    highlightItem(uid) {
        this.$('[data-uid]').each((i, node) => {
            const el = this.$(node);
            el.toggleClass('table__row_active', el.data('uid') === uid);
        });
    }

    @on('keydown')
    onKeyDown(e) {
        if(e.keyCode === KEY_DOWN || e.keyCode === KEY_UP) {
            const currentItem = this.$('.table__row_active');
            const nextItem = e.keyCode === KEY_DOWN ? currentItem.next() : currentItem.prev();
            if(nextItem.length > 0 && nextItem.attr('href')) {
                router.toUrl(nextItem.attr('href'));
                e.preventDefault();
            }
        }
    }

    @on('click [data-sort]')
    onSortClick(e) {
        const el = this.$(e.currentTarget);
        const sorting = this.getSettings();
        this.setSettings(updateSort(el.data('sort'), sorting));
        this.render();
    }
}

export default DataGridView;
