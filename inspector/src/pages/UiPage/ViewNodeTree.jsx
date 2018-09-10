import React, { Component } from "react";
import { bindActionCreators } from "redux";
import PropTypes from "prop-types";
import { connect } from "react-redux";
import { PageHeader, Panel, ListGroup, ListGroupItem } from "react-bootstrap";
import TreeView from "@components/TreeView";

import {
  /********* notice **********/
  NOTICE_ADD_SUCCESS,
  NOTICE_ADD_FAIL,

  /********* storage **********/
  STORAGE_ADD_UI_RECORD,
  STORAGE_LOAD_UI_ALL_RECORD,
  STORAGE_CHANGE_UI_RECORD_INFO,
  STORAGE_LOAD_UI_RECORD,
  STORAGE_REMOVE_UI_RECORD,

  /********* ui **********/
  UI_CHANGE_VIEW_NODE_ROOT,
  UI_LOCK,
  UI_UNLOCK,
  UI_HOVER,
  UI_CHANGE_HIGHLIGHT_VIEW_INFO
} from "@reducers";

import style from "./style.css";
import { genId } from "@reducers/util";

function createNodeDesc(nodeInfo) {
  var info = nodeInfo.info || nodeInfo;
  var desc = "";
  desc += "(";
  desc += info["index"] == undefined ? "?" : info["index"];
  desc += ") ";
  desc += (info["cls"] || "?").split(".").pop();
  var texts = [];
  if (info["text"] && info["text"].length > 0) {
    texts = texts.concat(info["text"]);
  }
  if (info["hint"] && info["hint"].length > 0) {
    texts = texts.concat(info["hint"]);
  }
  if (info["desc"] && info["desc"].length > 0) {
    texts = texts.concat(info["desc"]);
  }
  var text = texts.join(" ");
  if (text.length > 0) {
    desc += ":" + text;
  }
  desc += " ";
  var bounds = info["bounds_on_screen"] || {
    left: 0,
    top: 0,
    right: 0,
    bottom: 0
  };
  desc += "[" + bounds["left"] + "," + bounds["top"] + "]";
  desc += "[" + bounds["right"] + "," + bounds["bottom"] + "]";
  return desc;
}

function infoEqual(info1, info2) {
  if (info1 === null || info1 === undefined) return false;
  if (info2 === null || info2 === undefined) return false;
  return info1._id === info2._id;
}

const mapStateToProps = (state, ownProps) => {
  return {
    isLock: state.app.ui.isLock,
    highlightViewInfo: state.app.ui.highlightViewInfo,
    root: state.app.ui.root,
    isNeedFocusInThree: state.app.ui.isNeedFocusInThree
  };
};

const mapDispatchToProps = (dispatch, ownProps) => {
  return bindActionCreators(
    {
      /********* notice **********/
      showSuccess: NOTICE_ADD_SUCCESS,
      showError: NOTICE_ADD_FAIL,

      /********* storage **********/
      addRecord: STORAGE_ADD_UI_RECORD,
      loadAllRecord: STORAGE_LOAD_UI_ALL_RECORD,
      changeRecordInfo: STORAGE_CHANGE_UI_RECORD_INFO,
      loadRecord: STORAGE_LOAD_UI_RECORD,
      removeRecord: STORAGE_REMOVE_UI_RECORD,

      /********* ui **********/
      lock: UI_LOCK,
      unlock: UI_UNLOCK,
      hover: UI_HOVER,
      changeViewNodeRoot: UI_CHANGE_VIEW_NODE_ROOT,
      changeHighlightViewInfo: UI_CHANGE_HIGHLIGHT_VIEW_INFO
    },
    dispatch
  );
};
@connect(
  mapStateToProps,
  mapDispatchToProps
)
export default class ViewNodeTree extends Component {
  onClick(info) {
    const { isLock, highlightViewInfo } = this.props;
    if (!infoEqual(info, highlightViewInfo)) {
      if (isLock) this.props.unlock();
      this.props.changeHighlightViewInfo(info);
    }
    this.props.lock();
  }

  onHover(info) {
    // const { isLock, highlightViewInfo } = this.props;
    // if (!isLock) {
    //   if (!infoEqual(info, highlightViewInfo)) {
    //     this.props.changeHighlightViewInfo(info);
    //   }
    // }
  }

  updatePos() {
    const { isNeedFocusInThree } = this.props;
    if (!isNeedFocusInThree) return;

    const container = document.getElementById("view-node-tree");
    const targetNode = document.getElementById("highlight");
    // console.log("updatePos", container, targetNode);

    if (!container) return;
    if (!targetNode) return;

    var height = container.clientHeight;
    var sTop = targetNode.offsetTop - container.offsetTop;
    container.scrollTop = Math.max(0, sTop - height * 0.5);
  }

  componentDidUpdate() {
    this.updatePos();
  }

  componentDidMount() {
    this.updatePos();
  }

  renderNode(node, i, key) {
    const { isLock, highlightViewInfo } = this.props;
    const { info, children } = node;
    const self = this;
    const isHighlight = infoEqual(info, highlightViewInfo);
    const style = {};
    style.border = "1px solid transparent";
    if (isHighlight) {
      style.backgroundColor = "rgba(255,0,0,0.2)";
      if (isLock) {
        style.border = "1px dashed red";
      }
    }
    const nowKey = key !== undefined ? key + "." + i : i;
    const ret =
      children &&
      children.map((childNode, i) => self.renderNode(childNode, i, nowKey));
    const expand =
      ret == undefined
        ? false
        : ret.map(n => n.expand).reduce((b1, b2) => b1 || b2);
    const childrenUI = ret && ret.map(n => n.ui);
    const label = (
      <div
        {...(isHighlight ? { id: "highlight" } : {})}
        style={{ display: "inline-block" }}
        onClick={() => self.onClick(info)}
        onMouseOver={() => self.onHover(info)}
      >
        <span className="node" style={style}>
          {createNodeDesc(info)}
        </span>
      </div>
    );
    return {
      expand: isHighlight || expand,
      ui: (
        <TreeView key={nowKey} nodeLabel={label} defaultCollapsed={!expand}>
          {childrenUI}
        </TreeView>
      )
    };
  }

  render() {
    const { root } = this.props;
    const self = this;
    const childNode = root && self.renderNode(root, 0);

    return (
      <div
        id="view-node-tree"
        className={
          style.viewNodeTree +
          " " +
          (childNode && childNode.expand ? ".expand" : "")
        }
        ref={ref => self.container}
      >
        {childNode && childNode.ui}
      </div>
    );
  }
}
