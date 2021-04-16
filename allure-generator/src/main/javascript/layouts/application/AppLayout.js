import "./styles.scss";
import { View } from "backbone.marionette";
import ErrorSplashView from "../../components/error-splash/ErrorSplashView";
import SideNav from "../../components/side-nav/SideNavView";
import { className, regions } from "../../decorators";
import translate from "../../helpers/t";
import template from "./AppLayout.hbs";

@className("app")
@regions({
  content: ".app__content",
  nav: ".app__nav",
})
class AppLayout extends View {
  template = template;

  initialize() {}

  onRender() {
    this.showChildView("nav", new SideNav());
    const dataPromise = this.loadData();
    if (dataPromise) {
      dataPromise
        .then(() => {
          this.showChildView("content", this.getContentView());
          this.onViewReady();
        })
        .catch(() => {
          this.showChildView(
            "content",
            new ErrorSplashView({ code: 404, message: translate("errors.notFound") }),
          );
        });
    } else {
      this.showChildView("content", this.getContentView());
    }
  }

  onViewReady() {}

  loadData() {}

  getContentView() {
    throw new Error("attempt to call abstract method");
  }

  shouldKeepState() {
    return true;
  }
}

export default AppLayout;
