import {View} from 'backbone.marionette';
import {className, regions, ui} from '../../decorators';
import template from './TestResultOverviewView.hbs';
import pluginsRegistry from '../../utils/pluginsRegistry';
import StatusDetailsView from '../../blocks/status-details/StatusDetailsView';
import {Model} from 'backbone';
import ExecutionView from '../execution/ExecutionView';

@className('test-result-overview')
@ui({
    statusDetails: '.test-result-overview__status-details',
    execution: '.test-result-overview__execution',
})
@regions({
    statusDetails: '@ui.statusDetails',
    execution: '@ui.execution',
})
class TestResultOverviewView extends View {
    template = template;

    initialize() {
        this.blocks = [];
    }

    onRender() {
        const message = this.model.get('message');
        if (message) {
            const statusDetails = new Model({
                message: this.model.get('message'),
                trace: this.model.get('trace'),
                status: this.model.get('status')
            });
            this.showChildView('statusDetails', new StatusDetailsView({model: statusDetails}));
        }

        this.showBlock(this.$('.test-result-overview__tags'), pluginsRegistry.testResultBlocks.tag);
        this.showBlock(this.$('.test-result-overview__before'), pluginsRegistry.testResultBlocks.before);

        const testStage = this.model.get('testStage');
        if (testStage) {
            const execution = new Model({
                steps: testStage.steps,
                attachments: testStage.attachments
            });
            this.showChildView('execution', new ExecutionView({model: execution}));
        }

        this.showBlock(this.$('.test-result-overview__after'), pluginsRegistry.testResultBlocks.after);
    }

    onDestroy() {
        this.blocks.forEach(block => block.destroy());
    }

    showBlock(container, blocks) {
        blocks.forEach((Block) => {
            const block = new Block({model: this.model});
            block.$el.appendTo(container);
            this.blocks.push(block);
            block.render();
        });
    }

    templateContext() {
        return {
            cls: this.className
        };
    }
}

export default TestResultOverviewView;