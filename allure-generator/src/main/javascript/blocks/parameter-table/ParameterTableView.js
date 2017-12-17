import './styles.scss';
import {CollectionView, View} from 'backbone.marionette';
import {className, events, ui} from '../../decorators/index';
import rowTemplate from './ParameterRowView.hbs';

@className('parameter-table__row')
@ui({
    parameter: '.parameters-table__cell'
})
@events({
    'click @ui.parameter': 'onParameterClick'
})
class ParameterRowView extends View {
    template = rowTemplate;

    onParameterClick(e) {
        this.$(e.target).siblings().addBack().toggleClass('line-ellipsis');
    }
}

@className('parameter-table')
class ParameterTableView extends CollectionView {
    childView = ParameterRowView;
}

export default ParameterTableView;