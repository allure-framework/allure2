import './styles.css';
import {View} from 'backbone.marionette';
import $ from 'jquery';
import Sortable from 'sortablejs';
import settings from '../../util/settings';
import {className} from '../../decorators';
import pluginsRegistry from '../../util/pluginsRegistry';
//import {fetchAndShow} from '../../util/loading';


const widgetTpl = (id) => `<div class="widget island" data-id="${id}">
    <div class="widget__handle">
        <span class="draggable-icon"></span>
    </div>
    <div class="widget__body"></div>
</div>`;
const colTpl = '<div class="widgets-grid__col"></div>';

@className('widgets-grid')
class WidgetsGridView extends View {
    template = () => '';

    initialize() {
        this.widgets = pluginsRegistry.widgets[this.options.tabName];
    }

    onRender() {
        this.getWidgetsArrangement().map(col => {
            return col.map(widgetName => [widgetName, this.widgets[widgetName]]);
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
            return col.filter(widgetName => this.widgets[widgetName]);
        });
        Object.keys(this.widgets).forEach(widgetName => {
            if (storedWidgets.every(col => col.indexOf(widgetName) === -1)) {
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
       /// fetchAndShow(this, name, this.model, new Widget({model: this.model}))
        this.getRegion(name).show(new Widget({model: this.model}));
    }
}

export default WidgetsGridView;
