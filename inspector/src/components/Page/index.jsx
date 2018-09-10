import React, { Component } from "react";
import PropTypes from "prop-types";
import { connect } from "react-redux";
import { Tab, Nav, NavItem, Button } from "react-bootstrap";

import { Link } from "react-router-dom";

import style from "./style.css";
import { listenerCount } from "cluster";

export default class Page extends Component {
  static contextTypes = {
    router: PropTypes.shape({
      history: PropTypes.shape({
        push: PropTypes.func.isRequired,
        replace: PropTypes.func.isRequired,
        createHref: PropTypes.func.isRequired
      }).isRequired
    }).isRequired
  };

  onSelect = url => {
    const {
      router: {
        route: { location },
        history
      }
    } = this.context;
    if (url !== location) {
      history.replace(url);
    }
  };

  render() {
    const {
      router: {
        route: { location }
      }
    } = this.context;

    const NavButton = (url, title) => (
      <li className={style.navListItem}>
        <a
          className={
            url === location.pathname ? style.navBtnActive : style.navBtn
          }
          onClick={() => this.onSelect(url)}
        >
          {title}
        </a>
      </li>
    );

    return (
      <div className={style.container}>
        <div className={style.navbar}>
          <ul className={style.navList}>
            {NavButton("/", "通用设置")}
            {NavButton("/ui", "界面嗅探")}
            {NavButton("/msg", "模拟消息")}
          </ul>
        </div>
        <div className={style.content}>{this.props.children}</div>
      </div>
    );
  }
}
