import "./styles.scss";
import * as bem from "b_";
import * as React from "react";
import { ReportTabConfiguration } from "../../allure";
import { Link, NavLink } from "react-router-dom";
import Arrow from "../Arrow";

const b = bem.with("SideNavigation");

interface SideNavigationProps {
  tabs: Array<ReportTabConfiguration>;
}

interface SideNavigationState {
  expanded?: boolean;
}

export default class SideNavigation extends React.Component<
  SideNavigationProps,
  SideNavigationState
> {
  state = {
    expanded: false,
  };

  handleCollapseClick = () => {
    this.setState(prevState => ({
      expanded: !prevState.expanded,
    }));
  };

  render() {
    const { tabs } = this.props;
    const { expanded } = this.state;
    return (
      <div className={b("", { expanded })}>
        <div className={b("head")}>
          <Link className={b("brand")} to={"/"}>
            <span className={b("brand-text")}>Allure</span>
          </Link>
        </div>
        <ul className={b("menu")}>
          {tabs.map(({ id, name, icon }) => (
            <li className={b("item")} key={id}>
              <NavLink
                className={b("link")}
                activeClassName={b("link", { active: true })}
                to={`/${id}`}
                exact={!id.length}
              >
                <div className={b("icon")}>
                  <span className={icon || "fa fa-question"} />
                </div>
                <div className={b("text", { expanded })}>{name}</div>
              </NavLink>
            </li>
          ))}
        </ul>
        <div className={b("strut")} />
        <div className={b("footer")}>
          <div className={b("item")}>
            <div className={b("collapse")} onClick={this.handleCollapseClick}>
              <div className={b("icon")}>
                <Arrow expanded={expanded} />
              </div>
              <span className={b("text", { expanded })}>Collapse</span>
            </div>
          </div>
        </div>
      </div>
    );
  }
}
