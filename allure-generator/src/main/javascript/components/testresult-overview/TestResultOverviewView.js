import './styles.scss';
import {View} from 'backbone.marionette';
import {className, on} from '../../decorators';
import template from './TestResultOverviewView.hbs';
import pluginsRegistry from '../../util/pluginsRegistry';

@className('test-result-overview')
class TestResultOverviewView extends View {
    template = template;

    initialize() {
        this.blocks = [];
    }

    onRender() {
        this.showBlock(this.$('.test-result-overview__tags'), pluginsRegistry.testResultBlocks.tag);
        this.showBlock(this.$('.test-result-overview__before'), pluginsRegistry.testResultBlocks.before);
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

    @on('click .status-details__trace-toggle')
    onStacktraceClick(e) {
        this.$(e.currentTarget).closest('.status-details').toggleClass('status-details__expanded');
    }

    templateContext() {
        const statusDetails = this.model.get('statusDetails');
        return {
            cls: this.className,
            statusDetails: {
                ...statusDetails,
                message: statusDetails.message || 'Empty status details'
            }
        }
    }
}

export default TestResultOverviewView;