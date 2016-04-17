import './styles.css';
import {ItemView} from 'backbone.marionette';
import {className} from '../../decorators';
import template from './LinksView.hbs';

@className('pane__section')
class IssuesView extends ItemView {
    template = template;

    serializeData() {
        const {testId, issues} = this.model.attributes;
        return {
            hasLinks: testId || issues.length > 0,
            testId, issues
        };
    }
}

export default IssuesView;
