import React, { Component } from "react";
import PropTypes from "prop-types";
import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import {
  PageHeader,
  Panel,
  ListGroup,
  ListGroupItem,
  Button
} from "react-bootstrap";

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

import { sendMsg } from "@/net.js";

import HostUrl from "@components/HostUrl.jsx";
import ScreenPreview from "@components/ScreenPreview/";
import Notice from "@components/Notice";

import style from "./style.css";
import RecordSelector from "./RecordSelector.jsx";
import ViewInfo from "./ViewInfo.jsx";
import ViewNodeTree from "./ViewNodeTree.jsx";

import noneImg from "@assets/none.png";

const mapStateToProps = (state, ownProps) => {
  return {
    ...state.app.ui,
    ...state.app.storage.ui,
    url: state.app.storage.url
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
export default class UiPage extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  onDumpUI = () => {
    const { addRecord, showError, url } = this.props;
    const dumpUiUrl = url + "/dump_ui";
    sendMsg(dumpUiUrl, {})
      .success(function(body) {
        console.log("response:", body);
        if (body.result !== "success!") {
          console.log("base info error:", body);
          showError(JSON.stringify(body));
          return;
        }
        addRecord({
          info: {
            now: body.content.now,
            bank: body.content.bank,
            pkg: body.content.pkg,
            now_activity: body.content.now_activity,
            root_view_names: body.content.root_view_names
          },
          root: body.content.root,
          screencap: body.screencap
        });
      })
      .fail(function(error) {
        console.log("error:", error);
        showError(error.toString());
      });
  };

  onSelect = id => {
    this.props.loadRecord(id);
  };

  onDelete = id => {
    this.props.removeRecord(id);
  };

  onNewTag = (id, newTag) => {
    const { allRecordInfo } = this.props;
    this.props.changeRecordInfo({
      ...allRecordInfo[id],
      tag: newTag
    });
  };

  onClick = () => {
    const { isLock } = this.props;
    if (isLock) {
      this.props.unlock();
    } else {
      this.props.lock();
    }
  };

  onHover = (x, y, color) => {
    this.props.hover(x, y, color);
  };

  componentDidMount() {
    const { loadAllRecord } = this.props;
    loadAllRecord();
  }

  componentDidUpdate() {
    const {
      nowRecord,
      nowRecord: { root },
      changeViewNodeRoot,
      unlock,
      changeHighlightViewInfo
    } = this.props;
    // console.log("componentDidUpdate", this.props.nowRecord);

    if (root && nowRecord && root.id !== nowRecord.id) {
      const keys = Object.keys(root);
      if (keys && keys.length > 0) {
        root.id = nowRecord.id;
        changeViewNodeRoot(root);
        unlock();
        changeHighlightViewInfo(null);
      }
    }
  }

  render() {
    const {
      nowRecord: { id, info, screencap },
      highlightViewInfo,
      allRecordInfo,
      root,
      isLock
    } = this.props;
    const self = this;
    const img =
      screencap === null ? noneImg : "data:image/png;base64, " + screencap;

    var highlightRect = { x: 0, y: 0, width: 0, height: 0 };

    if (highlightViewInfo !== null) {
      const bounds = highlightViewInfo.bounds_on_screen;
      highlightRect = {
        x: bounds.left,
        y: bounds.top,
        width: bounds.right - bounds.left,
        height: bounds.bottom - bounds.top
      };
    }

    return (
      <div className={style.root}>
        <PageHeader>
          界面嗅探
          <p className="lead">
            检测和查看 App 界面元素的属性, 测试搜寻条件, 找色/找图/多点找色,
            输入/点击测试
          </p>
        </PageHeader>
        <div className={style.content}>
          <div className={style.left}>
            <div style={{ marginBottom: "10px" }}>
              <Button bsStyle="primary" onClick={self.onDumpUI}>
                抓取界面
              </Button>
            </div>
            <RecordSelector
              nowId={id}
              all={allRecordInfo}
              onSelect={self.onSelect}
              onDelete={self.onDelete}
              onNewTag={self.onNewTag}
            />
            <Notice countShowScroll={3} scrollHeight={250} />
          </div>
          <div className={style.mid}>
            <ScreenPreview
              src={img}
              isLock={isLock}
              rect={highlightRect}
              onClick={self.onClick}
              onHover={self.onHover}
            />
          </div>
          <div className={style.right}>
            <div className={style.rightTop}>
              <ViewNodeTree />
            </div>
            <div className={style.rightBottom}>
              <ViewInfo info={highlightViewInfo} />
            </div>
          </div>
        </div>
      </div>
    );
  }
}
