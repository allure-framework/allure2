import AppLayout from '../application/AppLayout';
import ErrorSplashView from '../../components/error-splash/ErrorSplashView';

export default class ErrorLayout extends AppLayout {
    getContentView() {
        const {code, message} = this.options;
        return new ErrorSplashView({code, message});
    }
}
