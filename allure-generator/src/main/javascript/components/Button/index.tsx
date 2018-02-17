import "./styles.scss";
import * as React from "react";
import * as bem from "b_";

const b = bem.with("Button");

export enum ButtonSize {
  Large = "large",
  Normal = "normal",
  Small = "small",
}

const Button: React.SFC<{ size?: ButtonSize; onClick?: () => void }> = ({
  size = ButtonSize.Normal,
  children,
  onClick,
}) => (
  <button className={b("", { size })} onClick={onClick}>
    {children}
  </button>
);

export default Button;
