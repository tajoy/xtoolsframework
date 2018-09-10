import React, { Component } from "react";
import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import { Alert, Button } from "react-bootstrap";
import { Motion, spring } from "react-motion";
import PropTypes from "prop-types";
import { Spring, Transition } from "react-spring";
import { Element, animateScroll as scroll, scroller } from "react-scroll";
import uuid from "uuid-v4";
import assert from "assert";

export const State = {
  SUCCESS: "success",
  WARNING: "warning",
  DANGER: "danger",
  INFO: "info"
};
import { NOTICE_REMOVE, NOTICE_CLEAR } from "@reducers";

const inlineStyleButton = {
  margin: 10
};

const mapStateToProps = (state, ownProps) => {
  return { items: [...state.app.notice.items] };
};

const mapDispatchToProps = (dispatch, ownProps) => {
  return bindActionCreators(
    {
      removeMsg: NOTICE_REMOVE,
      handleClear: NOTICE_CLEAR
    },
    dispatch
  );
};

@connect(
  mapStateToProps,
  mapDispatchToProps
)
export default class Notice extends React.Component {
  static propTypes = {
    countShowScroll: PropTypes.number,
    scrollHeight: PropTypes.number,
    maxCount: PropTypes.number
  };

  static defaultProps = {
    countShowScroll: 1,
    scrollHeight: 120,
    maxCount: 100
  };

  constructor(props) {
    super(props);
    this.containerId = uuid();
  }

  scrollToLastOne() {
    const { containerId } = this;
    const { items } = this.props;
    if ( items === null || items === undefined || items.length === 0) {
      return;
    }
    const name = "notice-alert-" + (items.length - 1);
    scroller.scrollTo(name, {
      duration: 0,
      delay: 0,
      smooth: false,
      containerId
    });
  }

  componentDidMount() {
    this.scrollToLastOne();
  }

  componentDidUpdate() {
    this.scrollToLastOne();
  }

  shouldComponentUpdate(nextProps, nextState) {
    if (this.props !== nextProps) return true;
    if (this.props === null && nextProps === null) return false;
    if (!assert.deepEqual(this.props, nextProps)) return true;

    if (this.state !== nextState) return true;
    if (this.state === null && nextState === null) return false;
    if (!assert.deepEqual(this.state, nextState)) return true;

    if (this.props.items !== nextProps.items) return true;
    if (this.props.items === null && nextProps.items === null) return false;

    const nowCount = this.props.items.length || 0;
    const nextCount = nextProps.items.length || 0;
    if (nowCount !== nextCount) return true;
    return false;
  }

  renderClearBtn() {
    const self = this;
    const {
      items,
      handleClear,
      removeMsg,
      countShowScroll,
      scrollHeight,
      maxCount
    } = this.props;

    if (items.length > countShowScroll) {
      return (
        <Button
          bsStyle="primary"
          onClick={handleClear}
          style={{
            // opacity: styles.opacity,
            // transformOrigin: "top left",
            // transform: "scaleY(" + styles.scaleY + ")",
            marginTop: "10px",
            marginBottom: "10px"
          }}
        >
          清空
        </Button>
      );
    } else {
      return null;
    }
  }

  renderMsgItem = (item, index) => {
    const { items, removeMsg, countShowScroll } = this.props;
    const name = "notice-alert-" + index;
    return (
      <Element name={name} key={index}>
        <Alert
          bsStyle={item.bsStyle}
          style={{
            padding: "5px 25px 5px 5px",
            fontSize: "11px",
            marginTop: "10px",
            marginBottom: "10px",
            marginLeft: items.length > countShowScroll ? "10px" : "inherit",
            marginRight: items.length > countShowScroll ? "10px" : "inherit"
          }}
          onDismiss={() => removeMsg(item.id)}
        >
          <strong>#{item.no}</strong>
          {item.text}
        </Alert>
      </Element>
    );
  };

  renderMsg() {
    const self = this;
    const { items, handleClear, scrollHeight, maxCount } = this.props;
    const count = items.length;
    if (count > maxCount) {
      const ignore = (
        <p
          style={{
            margin: "15px",
            fontSize: "11px",
            textAlign: "center",
            color: "gray"
          }}
        >
          .......省略
          <strong
            style={{
              textDecoration: "underline",
              fontSize: "13px"
            }}
          >
            {count - maxCount}
          </strong>
          个更多消息........
        </p>
      );
      return [
        ignore, // 插入到头部
        ...items
          // 过滤, 去掉前面的, 最保留后面 maxCount 个
          .filter((item, index) => index >= count - maxCount)
          // 渲染每一个消息
          .map(self.renderMsgItem)
      ];
    } else {
      return items.map(self.renderMsgItem);
    }
  }

  render() {
    const self = this;
    const { containerId } = this;
    const {
      items,
      handleClear,
      removeMsg,
      countShowScroll,
      scrollHeight,
      maxCount
    } = this.props;

    const style = {};
    if (items.length > countShowScroll) {
      style.height = scrollHeight;
      style.overflow = "auto";
      style.border = "1px solid #ddd";
      style.backgroundColor = "#eee";
    }

    return (
      <div>
        {this.renderClearBtn()}
        <div id={containerId} style={style}>
          {this.renderMsg()}
        </div>
      </div>
    );
  }
}
