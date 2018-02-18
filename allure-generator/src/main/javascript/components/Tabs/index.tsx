import "./styles.scss";
import * as React from "react";
import * as bem from "b_";
import { NavLink } from "react-router-dom";

const b = bem.with("Tabs");

interface Tab {
  href: string;
  name: string;
}

interface TabsProps {
  tabs: Array<Tab>;
  match?: any;
}

const Tabs: React.SFC<TabsProps> = ({ tabs, match }) => (
  <ul className={b()}>
    {tabs.map(({ href, name }) => (
      <li className={b("item")} key={name}>
        <NavLink
          className={"link link__no-decoration"}
          activeClassName={`${b("item")}_active`}
          to={`${match.url}${href}`}
          exact={true}
        >
          <span className={b("text")}>{name}</span>
        </NavLink>
      </li>
    ))}
  </ul>
);

export default Tabs;
