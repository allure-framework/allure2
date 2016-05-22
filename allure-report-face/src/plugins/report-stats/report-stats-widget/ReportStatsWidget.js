import './styles.css';
import {ItemView} from 'backbone.marionette';
import template from './ReportStatsWidget.hbs';
import {onModel} from '../../../decorators';
import ReportWidgetModel from './ReportWidgetModel';

export default class ReportStatsWidget extends ItemView {
    template = template;

    constructor() {
        super({
            model: new ReportWidgetModel() 
        });
    }
    
    initialize() {
        this.model.fetch();
    }
    
    @onModel('change')
    render() {
        super.render();
    }

    serializeData() {
        return {
            report: this.model.toJSON()
        };
    }
}
