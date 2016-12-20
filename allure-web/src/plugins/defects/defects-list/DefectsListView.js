import './styles.css';
import DataGridView from '../../../components/data-grid/DataGridView';
import {on} from '../../../decorators';
import router from '../../../router';
import template from './DefectsListView.hbs';

class DefectsListView extends DataGridView {
    template = template;
    settingsKey = 'defectsSettings';

    initialize({state}) {
        this.state = state;
        this.listenTo(this.state, 'change:defect', (model, defect) => this.highlightItem(defect));
    }

    onRender() {
        this.highlightItem(this.state.get('defect'));
    }

    @on('click .defects-list__item')
    onDefectClick(e) {
        const defectId = this.$(e.currentTarget).data('uid');
        router.to('defects/' + defectId);
    }

    serializeData() {
        return {
            sorting: this.getSettings(),
            defectTypes: this.collection.toJSON().map(type =>
                Object.assign({}, type, {
                    defects: this.applySort(type.defects)
                })
            )
        };
    }
}

export default DefectsListView;
