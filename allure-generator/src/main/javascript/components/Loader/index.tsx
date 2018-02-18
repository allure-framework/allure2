import "./styles.scss";
import * as React from "react";
import * as bem from "b_";

const b = bem.with("Loader");

const Loader: React.SFC = () => (
  <div className={b("strut")}>
    <div className={b("container")}>
      <span className="fa fa-circle-o-notch fa-spin fa-2x fa-fw" />
      <span className="sr-only">Loading...</span>
    </div>
  </div>
);

export default Loader;
