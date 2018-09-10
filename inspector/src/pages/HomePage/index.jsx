import electron from "electron";
import Promise from "bluebird";
import adb from "adbkit";

import React, { Component } from "react";
import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import {
  PageHeader,
  Panel,
  ListGroup,
  ListGroupItem,
  Button,
  Modal,
  Label,
  Alert,
  Overlay,
  Popover,
  Table,
  ButtonToolbar
} from "react-bootstrap";
import { Link } from "react-router-dom";

import {
  /********* storage **********/
  STORAGE_CLEAR
} from "@reducers";

import HostUrl from "@components/HostUrl.jsx";
import styles from "./styles.css";

const app = electron.app || electron.remote.app;

const mapStateToProps = (state, ownProps) => {
  return {};
};

const mapDispatchToProps = (dispatch, ownProps) => {
  return bindActionCreators(
    {
      /********* storage **********/
      clear: STORAGE_CLEAR
    },
    dispatch
  );
};

@connect(
  mapStateToProps,
  mapDispatchToProps
)
export default class HomePage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isShowPopover: false,
      targetPopover: null,
      devices: []
    };
    this.adb = adb.createClient();
    const self = this;
    this.adb
      .trackDevices()
      .then(function(tracker) {
        tracker.on("add", self.onDeviceAdd);
        tracker.on("remove", self.onDeviceRemove);
        tracker.on("end", function() {
          console.log("Tracking stopped");
        });
      })
      .catch(function(err) {
        console.error("Something went wrong:", err.stack);
      });
  }

  onDeviceAdd = device => {
    console.log("Device plugged", device);
    const { devices } = this.state;
    devices.push(device);
    this.setState({ devices: [...devices] });
  };

  onDeviceRemove = device => {
    console.log("Device unplugged", device);
    const { devices } = this.state;
    var indexOfDevice = -1;
    for (let i = 0; i < devices.length; i++) {
      const d = devices[i];
      if (d.id === device.id) {
        indexOfDevice = i;
        break;
      }
    }
    if (indexOfDevice >= 0) {
      devices.splice(indexOfDevice, 1);
      this.setState({ devices: [...devices] });
    }
  };

  grantPermission = () => {
    const { devices } = this.state;
    const self = this;
    const cmdList = [
      "settings put secure accessibility_enabled 0",
      "settings delete secure enabled_accessibility_services",
      "settings put secure enabled_accessibility_services x.tools.app/x.tools.app.AccessibilityService",
      "settings put secure accessibility_enabled 1"
    ];
    const cmd = cmdList.reduce((c1, c2) => c1 + " && " + c2);
    devices.map(device => {
      self.adb.shell(device.id, cmd);
    });
  };

  forwardPort = () => {
    const { devices } = this.state;
    const self = this;
    devices.map(device =>
      self.adb.forward(device.id, "tcp:12300", "tcp:12300")
    );
  };

  render() {
    const self = this;
    const { clear } = this.props;
    const { isShowPopover, targetPopover, devices } = this.state;
    const showConfirm = e => {
      this.setState({ isShowPopover: !isShowPopover, targetPopover: e.target });
    };
    const handleClose = () => this.setState({ isShowPopover: false });
    const handleClear = () => {
      clear();
      this.setState({ isShowPopover: false });
    };
    const userDataPath = app.getPath("userData");

    return (
      <div>
        <PageHeader>
          UI嗅探工具
          <p className="lead">{}</p>
        </PageHeader>
        <div>
          <Panel>
            <Panel.Heading>
              <Panel.Title componentClass="h3">内置常量</Panel.Title>
            </Panel.Heading>
            <Panel.Body>
              <Table striped bordered condensed hover>
                <thead>
                  <tr>
                    <th>名称</th>
                    <th>值</th>
                    <th>说明</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>process.env.NODE_ENV</td>
                    <td>{process.env.NODE_ENV}</td>
                    <td>构建类型</td>
                  </tr>
                  <tr>
                    <td>userData</td>
                    <td>{userDataPath}</td>
                    <td>数据储存路径</td>
                  </tr>
                </tbody>
              </Table>
            </Panel.Body>
          </Panel>
          <Panel>
            <Panel.Heading>
              <Panel.Title componentClass="h3">ADB工具</Panel.Title>
            </Panel.Heading>
            <Panel.Body>
              <p>设备列表</p>
              <Table striped bordered condensed hover>
                <thead>
                  <tr>
                    <th>#</th>
                    <th>ID</th>
                  </tr>
                </thead>
                <tbody>
                  {devices.map((device, i) => (
                    <tr key={i}>
                      <td>{i + 1}</td>
                      <td>{device.id}</td>
                    </tr>
                  ))}
                </tbody>
              </Table>
              <ButtonToolbar>
                {/*
                <Button bsStyle="primary" onClick={() => self.forwardPort()}>
                  转发23300端口
                </Button>
                <Button
                  bsStyle="primary"
                  onClick={() => self.grantPermission()}
                >
                  打开辅助功能权限
                </Button>
                */}
              </ButtonToolbar>
            </Panel.Body>
          </Panel>
          <Panel>
            <Panel.Heading>
              <Panel.Title componentClass="h3">常规设置</Panel.Title>
            </Panel.Heading>
            <Panel.Body>
              <Button bsStyle="danger" onClick={e => showConfirm(e)}>
                清除储存数据
              </Button>
              <Overlay
                show={isShowPopover}
                target={targetPopover}
                placement="right"
              >
                <Popover
                  id="popover-clear-confirm"
                  title={
                    <Label bsStyle="danger" style={{ fontSize: "1.4em" }}>
                      你确定要清楚所有数据吗?
                    </Label>
                  }
                >
                  <p>这将删除所有记录数据!!!</p>
                  <Button onClick={handleClear} bsStyle="primary">
                    确认
                  </Button>
                  <Button onClick={handleClose} style={{ marginLeft: "10px" }}>
                    取消
                  </Button>
                </Popover>
              </Overlay>
            </Panel.Body>
          </Panel>
          <HostUrl />
        </div>
      </div>
    );
  }
}
