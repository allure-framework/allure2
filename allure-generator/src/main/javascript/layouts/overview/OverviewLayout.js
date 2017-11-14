import AppLayout from '../application/AppLayout';
import WidgetsGridView from '../../components/widgets-grid/WidgetsGridView';

export default class OverviewLayout extends AppLayout {

    getContentView() {
        return new WidgetsGridView({tabName: 'widgets'});
    }
}
