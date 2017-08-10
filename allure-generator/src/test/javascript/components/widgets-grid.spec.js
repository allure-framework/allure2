import {View} from 'backbone';
import pluginsRegistry from 'utils/pluginsRegistry';
import WidgetsGridView from 'components/widgets-grid/WidgetsGridView';
import WidgetsModel from 'data/widgets/WidgetsModel';
import {getSettingsForWidgetGridPlugin} from 'utils/settingsFactory';

describe('WidgetsGridView', function() {
    let settings = getSettingsForWidgetGridPlugin('ALLURE_TEST');
    function PageObject(el) {
        this.column = (i) => el.find('.widgets-grid__col').eq(i);
        this.widgetsAtCol = (i) => this.column(i).find('.widget');
    }

    beforeEach(() => {
        settings = getSettingsForWidgetGridPlugin('ALLURE_TEST');
        pluginsRegistry.widgets = {
            group: {
                a: View,
                b: View,
                c: View,
                d: View,
                e: View
            }
        };
        this.model = new WidgetsModel({
            plugins: {
                a: [],
                b: {},
                c: {}
            }
        });
        this.view = new WidgetsGridView({model: this.model, tabName: 'group', settings: settings}).render();
        this.view.onRender();
        this.el = new PageObject(this.view.$el);
    });

    it('should render widgets by columns', () => {
        expect(this.el.widgetsAtCol(0)).toHaveLength(3);
        expect(this.el.widgetsAtCol(1)).toHaveLength(2);
    });

    it('should arrange widgets by default', () => {
        expect(this.view.getWidgetsArrangement()).toEqual([
            ['a', 'c', 'e'],
            ['b', 'd']
        ]);
    });

    it('should arrange widgets by saved value', () => {
        settings.set('widgets', [['a', 'b', 'c', 'd'], ['e']]);
        expect(this.view.getWidgetsArrangement()).toEqual([
            ['a', 'b', 'c', 'd'],
            ['e']
        ]);
    });

    it('should add remaining widgets and ignore missing', () => {
        settings.setWidgetsArrangement([['a', 'x', 'c'], ['d', 'e']]);
        expect(this.view.getWidgetsArrangement()).toEqual([
            ['a', 'c', 'b'],
            ['d', 'e']
        ]);
    });
});
