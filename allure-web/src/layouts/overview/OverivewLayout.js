import AppLayout from '../application/AppLayout';
import WidgetsModel from '../../data/widgets/WidgetsModel';
import WidgetsGridView from '../../components/widgets-grid/WidgetsGridView';

export default class OverivewLayout extends AppLayout {

    initialize() {
        this.model = new WidgetsModel();
    }

    loadData() {
        return this.model.fetch();
    }

    getContentView() {
        return new WidgetsGridView({model: this.model});
    }
}
