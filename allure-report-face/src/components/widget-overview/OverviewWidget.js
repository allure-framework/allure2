import './styles.css';
import {ItemView} from 'backbone.marionette';
import template from './OverviewWidget.hbs';

export default class OverviewWidget extends ItemView {
    template = template;
}
