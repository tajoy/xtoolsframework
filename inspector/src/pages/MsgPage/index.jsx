import React, { Component } from "react";
import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import {
  PageHeader,
  Button,
  Collapse,
  Popover,
  OverlayTrigger,
  Overlay,
  Label,
  ControlLabel,
  FormGroup,
  FormControl,
  HelpBlock,
  Panel,
  Tooltip,
  Glyphicon,
  ButtonToolbar,
  ButtonGroup,
  SplitButton,
  MenuItem,
  ProgressBar
} from "react-bootstrap";
import { Motion, spring } from "react-motion";
import Form from "react-jsonschema-form";
import NumericInput from "react-numeric-input";

import HostUrl from "@components/HostUrl.jsx";
import Notice from "@components/Notice";
import {
  /********* notice **********/
  NOTICE_ADD_SUCCESS,
  NOTICE_ADD_FAIL,

  /********* storage **********/
  STORAGE_CLEAR,
  STORAGE_LOAD_URL,
  STORAGE_CHANGE_URL,
  STORAGE_LOAD_MSG_ALL_DATA,
  STORAGE_CHANGE_MSG_ID,
  STORAGE_CHANGE_MSG_BASE_INFO,
  STORAGE_CHANGE_MSG_DATA,
  STORAGE_CHANGE_MSG_SEND_COUNT,
  STORAGE_CHANGE_MSG_SEND_INTERVAL
} from "@reducers";

import { sendMockMsg } from "@/net.js";

import style from "./style.css";
import {
  schemaDefines,
  schemaBaseInfo,
  uiSchema,
  uiSchemaBaseInfo
} from "./schema.js";
import { clearInterval } from "timers";

const mapStateToProps = (state, ownProps) => {
  return {
    ...state.app.msg,
    ...state.app.storage.msg,
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
      clear: STORAGE_CLEAR,
      loadAllData: STORAGE_LOAD_MSG_ALL_DATA,
      changeId: STORAGE_CHANGE_MSG_ID,
      changeBaseInfo: STORAGE_CHANGE_MSG_BASE_INFO,
      changeData: STORAGE_CHANGE_MSG_DATA,
      changeSendCount: STORAGE_CHANGE_MSG_SEND_COUNT,
      changeSendInterval: STORAGE_CHANGE_MSG_SEND_INTERVAL
    },
    dispatch
  );
};

@connect(
  mapStateToProps,
  mapDispatchToProps
)
export default class MsgPage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isShowBaseInfo: false,
      timer: null,
      sendCount: 2,
      sendingCount: 0,
      sendInterval: 0.1, // 秒
      sendingPercent: 0,
      sendingStartTime: 0
    };
    this.lastTimeUpdate = 0;
  }

  componentDidMount() {
    this.props.loadAllData();
  }

  onBaseInfoChange(obj) {
    console.log("onBaseInfoChange", obj);
    this.props.changeBaseInfo(obj.formData);
  }

  onBaseInfoError(errors) {
    console.log("onBaseInfoError", errors);
  }

  onBaseInfoSubmit(obj) {
    console.log("onBaseInfoSubmit", obj);
    const { dispatch } = this.props;
    this.setState({ isShowBaseInfo: false });
  }

  onChange(obj) {
    const id = obj.formData.id;
    const data = obj.formData.data;
    console.log("onChange", id, data);
    this.props.changeData(obj.formData);
  }

  onError(errors) {
    console.log("onError", errors);
  }
  onSubmit(obj) {
    console.log("onSubmit", obj);
  }

  send() {
    const { url, baseInfo, formData } = this.props;
    const self = this;

    const baseInfoEvent = {
      id: 77,
      data: baseInfo
    };

    sendMockMsg(url, baseInfoEvent)
      .success(function(body) {
        console.log("base info response:", body);
        if (body.result !== "success!") {
          console.log("base info error:", body);
          self.props.showError(JSON.stringify(body));
          return;
        }
        sendMockMsg(url, formData)
          .success(function(body) {
            console.log("msg response:", body);
            if (body.result !== "success!") {
              console.log("msg error:", body);
              self.props.showError(JSON.stringify(body));
              return;
            }
            self.props.showSuccess(JSON.stringify(body));
          })
          .fail(function(error) {
            console.log("msg error:", error);
            self.props.showError(error.toString());
          });
      })
      .fail(function(error) {
        console.log("base info error:", error);
        self.props.showError(error.toString());
      });
  }

  onClickSend = () => {
    this.send();
  };

  onClickSendBaseInfo = () => {
    const self = this;
    const { url, baseInfo } = this.props;
    const baseInfoEvent = {
      id: 77,
      data: baseInfo
    };
    sendMockMsg(url, baseInfoEvent)
      .success(function(body) {
        console.log("base info response:", body);
        if (body.result !== "success!") {
          console.log("base info error:", body);
          self.props.showError(JSON.stringify(body));
          return;
        }
        self.props.showSuccess(JSON.stringify(body));
      })
      .fail(function(error) {
        console.log("base info error:", error);
        self.props.showError(error.toString());
      });
  };

  stopBatchSending() {
    const { timer } = this.state;
    if (timer !== null) {
      // console.log("stop timer", timer);
      // clearInterval(timer);
      this.setState({ timer: null });
    }
  }

  onTimerCallback = () => {
    const { sendCount, sendInterval } = this.props;
    const { timer, sendingCount } = this.state;
    const nowCount = sendingCount + 1;

    if (nowCount <= sendCount && timer !== null) {
      this.send();
      this.setState({ sendingCount: nowCount });
    } else {
      this.stopBatchSending();
    }
  };

  onAnimationCallback = time => {
    const { timer } = this.state;
    if (timer === null) {
      return;
    }
    const { sendCount, sendInterval } = this.props;
    const { sendingStartTime, sendingCount } = this.state;
    const duration = time - sendingStartTime;
    const interval = sendInterval * 1000.0;
    const sendingPercent = (duration % interval) / interval;
    if (time - this.lastTimeUpdate > 100) {
      this.setState({ sendingPercent });
      this.lastTimeUpdate = time;
    }
    if (time - sendingStartTime > sendingCount * interval) {
      this.onTimerCallback();
    }
    if (timer !== null) {
      requestAnimationFrame(this.onAnimationCallback);
    }
  };

  onClickBatchSend = () => {
    const { sendCount, sendInterval } = this.props;
    const { timer } = this.state;
    if (timer !== null) {
      clearInterval(timer);
    }
    requestAnimationFrame(this.onAnimationCallback);
    // const t = setInterval(this.onTimerCallback, sendInterval * 1000.0);
    // console.log("send timer", t);
    this.setState({
      sendingCount: 0,
      timer: true,
      sendingStartTime: performance.now()
    });
  };

  onClickStopBatchSend = () => {
    this.stopBatchSending();
  };

  render() {
    const self = this;
    const {
      isShowBaseInfo,
      timer,
      sendingCount,
      sendingPercent,
      sendingStartTime
    } = this.state;
    const {
      id,
      formData,
      baseInfo,
      sendCount,
      sendInterval,
      changeSendCount,
      changeSendInterval
    } = this.props;
    const { bankType, tel, cardHolder, bankAccount, testing } = baseInfo;
    const isBatchSending = timer !== null;

    const schema = schemaDefines[id];
    const preview = JSON.stringify([baseInfo, formData], null, 2);

    uiSchema["ui:disabled"] = isBatchSending;
    uiSchemaBaseInfo["ui:disabled"] = isBatchSending;

    const styleNumInput = {
      wrap: {
        padding: "0px",
        marginLeft: "5px"
      },
      input: {
        padding: "0px 3.4ex 0px 3.4ex",
        marginLeft: "0px",
        borderRadius: "2px",
        color: "#555"
      }
    };
    console.log(this);
    

    const sendingProgress = 100 * ((sendingCount + sendingPercent) / sendCount);
    return (
      <div className={style.container}>
        <PageHeader>
          消息模拟
          <p className="lead">
            模拟服务器发送过来的消息, 将其发送至工具进行测试.
          </p>
        </PageHeader>
        <div className={style.container}>
          <div className={style.form}>
            {isBatchSending ? null : (
              <div>
                <Panel>
                  <OverlayTrigger
                    placement="right"
                    overlay={<Tooltip id="tooltip">点击展开</Tooltip>}
                  >
                    <Panel.Heading
                      style={{ fontSize: "1.3em" }}
                      onClick={() =>
                        this.setState({ isShowBaseInfo: !isShowBaseInfo })
                      }
                    >
                      {!isShowBaseInfo ? (
                        <div>
                          <Label bsStyle="primary">
                            <Glyphicon glyph="plus" />基础信息
                          </Label>{" "}
                          {testing ? (
                            <Label bsStyle="success">
                              <Glyphicon glyph="ok" />
                            </Label>
                          ) : (
                            <Label bsStyle="danger">
                              <Glyphicon glyph="remove" />
                            </Label>
                          )}{" "}
                          <Label bsStyle="primary">{bankType}</Label>{" "}
                          <Label bsStyle="primary">{tel}</Label>{" "}
                          <Label bsStyle="primary">{cardHolder}</Label>{" "}
                          <Label bsStyle="primary">{bankAccount}</Label>
                        </div>
                      ) : (
                        <Label bsStyle="primary">
                          <Glyphicon glyph="minus" />基础信息
                        </Label>
                      )}
                    </Panel.Heading>
                  </OverlayTrigger>
                  <Collapse in={isShowBaseInfo}>
                    <Panel.Body>
                      <Form
                        schema={schemaBaseInfo}
                        uiSchema={uiSchemaBaseInfo}
                        onChange={o => self.onBaseInfoChange(o)}
                        onSubmit={o => self.onBaseInfoSubmit(o)}
                        onError={e => self.onBaseInfoError(e)}
                        formData={baseInfo}
                      >
                        <div style={{ display: "none" }}>{isBatchSending}</div>
                        <Button type="submit" bsStyle="primary">
                          保存
                        </Button>
                      </Form>
                    </Panel.Body>
                  </Collapse>
                </Panel>
              </div>
            )}
            <div>
              <Collapse in={!isShowBaseInfo}>
                <Panel>
                  <Panel.Heading>
                    {isBatchSending ? "批量发送中" : "消息对象"}
                  </Panel.Heading>
                  <Panel.Body>
                    {isBatchSending ? null : (
                      <Form
                        schema={schema}
                        uiSchema={uiSchema}
                        onChange={o => self.onChange(o)}
                        onSubmit={o => self.onSubmit(o)}
                        onError={e => self.onError(e)}
                        formData={formData}
                      >
                        <div style={{ display: "none" }}>{isBatchSending}</div>
                        <Button style={{ display: "none" }} />
                      </Form>
                    )}
                  </Panel.Body>
                  <Panel.Footer>
                    {isBatchSending ? (
                      <div>
                        <ButtonToolbar>
                          <Button
                            bsStyle="danger"
                            onClick={self.onClickStopBatchSend}
                          >
                            停止发送
                          </Button>
                        </ButtonToolbar>
                        <label>
                          {sendingCount}/{sendCount}
                        </label>
                        <ProgressBar
                          active
                          bsStyle="success"
                          now={sendingProgress}
                        />
                      </div>
                    ) : (
                      <ButtonToolbar>
                        <SplitButton
                          bsStyle="primary"
                          title="发送"
                          onClick={self.onClickSend}
                          id="send-mock-event-button"
                        >
                          <MenuItem onClick={self.onClickSendBaseInfo}>
                            仅发送基础信息
                          </MenuItem>
                        </SplitButton>
                        <Button
                          bsStyle="primary"
                          onClick={self.onClickBatchSend}
                        >
                          批量定时发送
                        </Button>
                        <div className={"btn btn-primary " + style.fakeButton}>
                          数量<NumericInput
                            size={6}
                            mobile
                            min={2}
                            max={999}
                            value={sendCount}
                            onChange={v => changeSendCount(v)}
                            format={s => s + "次"}
                            precision={0.1}
                            style={styleNumInput}
                          />
                        </div>
                        <div className={"btn btn-primary " + style.fakeButton}>
                          间隔<NumericInput
                            size={8}
                            mobile
                            min={0.01}
                            max={9999}
                            value={sendInterval}
                            onChange={v => changeSendInterval(v)}
                            format={s => s + "秒"}
                            style={styleNumInput}
                          />
                        </div>
                      </ButtonToolbar>
                    )}
                    <Notice />
                  </Panel.Footer>
                </Panel>
              </Collapse>
            </div>
          </div>
          <div className={style.preview}>
            <p>预览</p>
            <textarea
              id="preview"
              className={style.previewTextArea}
              readOnly
              value={preview}
            />
          </div>
        </div>
      </div>
    );
  }
}
