import TestcaseModel from '../data/testcase/TestcaseModel';
import TestcaseView from '../components/testcase/TestcaseView';
import AttachmentView from '../components/attachment/AttachmentView';
import ErrorSplashView from '../components/error-splash/ErrorSplashView';
import t from '../helpers/t';

export default class TestcasePanes {
    constructor(state, paneView) {
        this.state = state;
        this.paneView = paneView;
        this.testcase = new TestcaseModel();
    }

    updatePanes(baseUrl, changed) {
        const testcaseUid = this.state.get('testcase');
        if (this.testcase.id !== testcaseUid) {
            this.testcase.clear();
            if (testcaseUid) {
                this.testcase.set({uid: testcaseUid});
                this.testcase.fetch().then(() => this.updatePanes(baseUrl, changed));
                return;
            }
        }
        this.paneView.updatePane('testcase', changed, () => new TestcaseView({
            baseUrl,
            state: this.state,
            model: this.testcase
        }));

        const attachment = this.testcase.getAttachment(changed.attachment);
        if (attachment) {
            this.paneView.updatePane('attachment', changed, () =>
                new AttachmentView({
                    baseUrl: baseUrl + '/' + this.state.get('testcase'),
                    attachment: attachment,
                    state: this.state
                })
            );
        } else {
            this.paneView.updatePane('attachment', changed, () =>
                new ErrorSplashView({code: 404, message: t('errors.missedAttachment', {})})
            );
        }
        this.paneView.updatePanesPositions();
    }

}
