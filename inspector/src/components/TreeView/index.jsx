import React from "react";
import PropTypes from "prop-types";

import style from "./style.css";

import { Collapse } from "react-bootstrap";

export default class TreeView extends React.PureComponent {
  static propTypes = {
    refContainer: PropTypes.func,
    collapsed: PropTypes.bool,
    defaultCollapsed: PropTypes.bool,
    nodeLabel: PropTypes.node.isRequired,
    className: PropTypes.string,
    itemClassName: PropTypes.string,
    childrenClassName: PropTypes.string,
    treeViewClassName: PropTypes.string
  };

  constructor(props) {
    super(props);
    const { defaultCollapsed } = this.props;
    this.state = {
      collapsed: defaultCollapsed ? null : false
    };
    this.handleClick = this.handleClick.bind(this);
  }

  handleClick(...args) {
    const { defaultCollapsed } = this.props;
    const { collapsed } = this.state;
    if (collapsed == null) {
      this.setState({ collapsed: !defaultCollapsed });
    } else {
      this.setState({ collapsed: !collapsed });
    }
    if (this.props.onClick) {
      this.props.onClick(...args);
    }
  }

  render() {
    const {
      className = "",
      itemClassName = "",
      treeViewClassName = "",
      childrenClassName = "",
      nodeLabel,
      children,
      defaultCollapsed,
      refContainer = ref => {},
      ...rest
    } = this.props;
    var { collapsed } = this.state;
    if (collapsed === null) {
      collapsed = defaultCollapsed;
    }

    let arrowClassName = style.arrow;
    let containerClassName = style.children;
    if (collapsed) {
      arrowClassName += " " + style.arrowCollapsed;
    }

    const arrow =
      children !== undefined && children.length > 0 ? (
        <div
          {...rest}
          className={className + " " + arrowClassName}
          onClick={this.handleClick}
        />
      ) : null;

    return (
      <div ref={refContainer} className={style.view + treeViewClassName}>
        <div className={style.item + itemClassName}>
          {arrow}
          {nodeLabel}
        </div>
        {children !== undefined && !collapsed ? (
          <div className={containerClassName + " " + childrenClassName}>
            {children}
          </div>
        ) : null}
      </div>
    );
  }
}
