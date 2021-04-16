import "./styles.scss";
import { View } from "backbone.marionette";
import { className } from "../../decorators";
import template from "./ErrorSplashView.hbs";

@className("error-splash")
class ErrorSplashView extends View {
  template = template;

  serializeData() {
    return {
      cls: this.className,
      code: this.options.code,
      message: this.options.message,
    };
  }
}

export default ErrorSplashView;
