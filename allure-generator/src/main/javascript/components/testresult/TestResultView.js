import './styles.css';
import {View} from 'backbone.marionette';
import {on, regions, behavior} from '../../decorators';
import pluginsRegistry from '../../util/pluginsRegistry';
import template from './TestResultView.hbs';
import ExecutionView from '../execution/ExecutionView';
import copy from '../../util/clipboard';

const SEVERITY_ICONS = {
    blocker: 'fa fa-exclamation-triangle',
    critical: 'fa fa-exclamation',
    normal: 'fa fa-file-o',
    minor: 'fa fa-arrow-down',
    trivial: 'fa fa-long-arrow-down'
};

@behavior('TooltipBehavior', {position: 'left'})
@regions({
    execution: '.testresult__execution'
})
class TestResultView extends View {
    template = template;

    initialize({state}) {
        this.state = state;
        this.plugins = [];
    }

    onRender() {
        this.showTestResultPlugins(this.$('.testresult__content_tags'), pluginsRegistry.testResultBlocks.tag);
        this.showTestResultPlugins(this.$('.testresult__content_before'), pluginsRegistry.testResultBlocks.before);
        this.showChildView('execution', new ExecutionView({
            baseUrl: '#testresult/' + this.model.id,
            state: this.state,
            model: this.model
        }));
        this.showTestResultPlugins(this.$('.testresult__content_after'), pluginsRegistry.testResultBlocks.after);
    }

    onDestroy() {
        this.plugins.forEach(plugin => plugin.destroy());
    }

    showTestResultPlugins(container, plugins) {
        plugins.forEach((Plugin) => {
            const plugin = new Plugin({model: this.model});
            plugin.$el.appendTo(container);
            this.plugins.push(plugin);
            plugin.render();
        });
    }

    serializeData() {
        return Object.assign({
            severityIcon: SEVERITY_ICONS[this.model.get('severity')],
            statusName: `status.${this.model.get('status')}`
        }, super.serializeData());
    }

    @on('click .testresult__trace-toggle')
    onStacktraceClick() {
        this.$('.testresult__failure').toggleClass('testresult__failure_expanded');
    }

    @on('click .fullname__body')
    onFillNameBopyClick() {
        this.$('.pane__subtitle').toggleClass('line-ellipsis', false);
    }

    @on('click .fullname__copy')
    onFullNameCopyClick() {
        copy(this.model.get('fullName'));
    }
}

export default TestResultView;
