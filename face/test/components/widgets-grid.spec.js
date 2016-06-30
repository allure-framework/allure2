import {View} from 'backbone';
import settings from 'util/settings';
import allurePlugins from 'pluginApi';
import WidgetsGridView from 'components/widgets-grid/WidgetsGridView';
import WidgetsModel from 'data/widgets/WidgetsModel';

describe('WidgetsGridView', function() {
    function PageObject(el) {
        this.column = (i) => el.find('.widgets-grid__col').eq(i);
        this.widgetsAtCol = (i) => this.column(i).find('.widget');
        this.widgetById = (id) => el.find(`[data-id=${id}]`);
    }

    beforeEach(function() {
        settings.clear();
        allurePlugins.widgets = {
            a: View,
            b: View,
            c: View,
            d: View,
            e: View
        };
        this.model = new WidgetsModel({
            plugins: {
                a: [],
                b: {},
                c: {}
            }
        });
        this.view = new WidgetsGridView({model: this.model}).render();
        this.view.onShow();
        this.el = new PageObject(this.view.$el);
    });

    it('should render widgets by columns', function() {
        expect(this.el.widgetsAtCol(0)).toHaveLength(3);
        expect(this.el.widgetsAtCol(1)).toHaveLength(2);
    });

    it('should arrange widgets by default', function() {
        expect(this.view.getWidgetsArrangement()).toEqual([
            ['a', 'c', 'e'],
            ['b', 'd']
        ]);
    });

    it('should arrange widgets by saved value', function() {
        settings.set('widgets', [['a', 'b', 'c', 'd'], ['e']]);
        expect(this.view.getWidgetsArrangement()).toEqual([
            ['a', 'b', 'c', 'd'],
            ['e']
        ]);
    });

    it('should add remaining widgets and ignore missing', function() {
        settings.set('widgets', [['a', 'x', 'c'], ['d', 'e']]);
        expect(this.view.getWidgetsArrangement()).toEqual([
            ['a', 'c', 'b'],
            ['d', 'e']
        ]);
    });


    it('should save current arrangement', function() {
        const widget = this.el.widgetById('b');
        this.el.widgetById('a').append(widget);
        this.view.saveWidgetsArrangement();
        expect(settings.get('widgets')).toEqual([
            ['a', 'b', 'c', 'e'],
            ['d']
        ]);
    });
});
