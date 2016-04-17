import './styles.css';
import {LayoutView} from 'backbone.marionette';
import $ from 'jquery';
import Sortable from 'sortablejs';
import settings from '../../util/settings';
import {className} from '../../decorators';
import allurePlugins from '../../pluginApi';

const widgetTpl = (id) => `<div class="widget island" data-id="${id}">
    <div class="widget__handle fa fa-arrows"></div>
    <div class="widget__body"></div>
</div>`;
const colTpl = `<div class="widgets-grid__col"></div>`;

@className('widgets-grid')
class WidgetsGridView extends LayoutView {
    template() {
        return '';
    }

    onRender() {
        this.getWidgetsArrangement().map(col => {
            return col.map(widgetName => [widgetName, allurePlugins.widgets[widgetName]]);
        }).forEach(widgetCol => {
            const col = $(colTpl);
            this.$el.append(col);
            widgetCol.forEach(([name, Widget]) => {
                this.addWidget(col, name, Widget);
            });
        });
        this.$('.widgets-grid__col').each((i, colEl) => new Sortable(colEl, {
            group: 'widgets',
            ghostClass: 'widget_ghost',
            handle: '.widget__handle',
            onEnd: this.saveWidgetsArrangement.bind(this)
        }));
    }

    getWidgetsArrangement() {
        const savedData = settings.get('widgets') || [[], []];
        const storedWidgets = savedData.map(col => {
            return col.filter(widgetName => allurePlugins.widgets[widgetName]);
        });
        Object.keys(allurePlugins.widgets).forEach(widgetName => {
            if(storedWidgets.every(col => col.indexOf(widgetName) === -1)) {
                const freeColumn = storedWidgets.reduce((smallestCol, col) =>
                    col.length < smallestCol.length ? col : smallestCol
                );
                freeColumn.push(widgetName);
            }
        });
        return storedWidgets;
    }

    saveWidgetsArrangement() {
        settings.save('widgets', this.$('.widgets-grid__col').toArray().map(colEl => {
            return $(colEl).find('.widget').toArray().map(el => $(el).data('id'));
        }));
    }

    addWidget(col, name, Widget) {
        const el = $(widgetTpl(name));
        col.append(el);
        this.addRegion(name, {el: el.find('.widget__body')});
        this.getRegion(name).show(new Widget({model: this.model.getWidgetData(name)}));
    }
}

export default WidgetsGridView;
