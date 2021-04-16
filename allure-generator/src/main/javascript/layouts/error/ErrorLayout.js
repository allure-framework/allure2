import ErrorSplashView from "../../components/error-splash/ErrorSplashView";
import AppLayout from "../application/AppLayout";

class ErrorLayout extends AppLayout {
  getContentView() {
    const { code, message } = this.options;
    return new ErrorSplashView({ code, message });
  }
}

export { ErrorLayout };
