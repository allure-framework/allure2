import { App } from "./app/index.mts";
import "./features/shell/index.mts";

import "./shared/styles/primitives/arrow/styles.scss";
import "./shared/styles/primitives/executor-icon/styles.scss";
import "./shared/styles/primitives/status-details/styles.scss";
import "./shared/styles/primitives/table/styles.scss";
import "./shared/styles/primitives/tabs/styles.scss";
import "./shared/styles/primitives/pane/styles.scss";

document.addEventListener("DOMContentLoaded", () => App.start());
