import { Behavior } from "backbone.marionette";

export default class LoadBehavior extends Behavior {
  initialize() {
    const renderView = this.view.render.bind(this.view);
    this.loaded = false;
    this.view.render = () => {
      if (this.loaded) {
        renderView();
      } else {
        this.view.loadData().then(() => {
          this.loaded = true;
          renderView();
        });
      }
    };
  }
}
