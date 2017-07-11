import TestResultModel from '../data/testresult/TestResultModel';
import TestResultView from '../components/testresult/TestResultView';
import AttachmentView from '../components/attachment/AttachmentView';
import ErrorSplashView from '../components/error-splash/ErrorSplashView';
import translate from '../helpers/t';

export default class TestResultPanes {
    constructor(state, paneView) {
        this.state = state;
        this.paneView = paneView;
        this.testResult = new TestResultModel();
    }

    updatePanes(baseUrl, changed) {
        const testResultUid = this.state.get('testResult');
        if (this.testResult.id !== testResultUid) {
            this.testResult.clear();
            if (testResultUid) {
                this.testResult.set({uid: testResultUid});
                this.testResult.fetch().then(() => this.updatePanes(baseUrl, changed));
                return;
            }
        }
        this.paneView.updatePane('testResult', changed, () => new TestResultView({
            baseUrl,
            state: this.state,
            model: this.testResult
        }));

        const attachment = this.testResult.getAttachment(changed.attachment);
        if (attachment) {
            this.paneView.updatePane('attachment', changed, () =>
                new AttachmentView({
                    baseUrl: baseUrl + '/' + this.state.get('testResult'),
                    attachment: attachment,
                    state: this.state
                })
            );
        } else {
            this.paneView.updatePane('attachment', changed, () =>
                new ErrorSplashView({code: 404, message: translate('errors.missedAttachment', {})})
            );
        }
        this.paneView.updatePanesPositions();
    }

}
