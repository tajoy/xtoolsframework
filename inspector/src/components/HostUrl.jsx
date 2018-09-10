import React, { Component } from "react";
import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import {
  Label,
  ControlLabel,
  FormGroup,
  FormControl,
  HelpBlock,
  Panel
} from "react-bootstrap";

import { STORAGE_LOAD_URL, STORAGE_CHANGE_URL } from "@reducers";

const mapStateToProps = (state, ownProps) => {
  return {
    url: state.app.storage.url
  };
};

const mapDispatchToProps = (dispatch, ownProps) => {
  return bindActionCreators(
    {
      /********* storage **********/
      loadUrl: STORAGE_LOAD_URL,
      changeUrl: STORAGE_CHANGE_URL
    },
    dispatch
  );
};

@connect(
  mapStateToProps,
  mapDispatchToProps
)
export default class HostUrl extends Component {
  componentDidMount() {
    this.props.loadUrl();
  }

  handleUrlChanged(url) {
    this.props.changeUrl(url);
  }

  render() {
    const { url } = this.props;
    return (
      <Panel>
        <Panel.Heading>受控端地址</Panel.Heading>
        <Panel.Body>
          <form onSubmit={e => e.preventDefault()}>
            <FormGroup controlId="formBasicText">
              <FormControl
                type="text"
                format="url"
                value={url}
                placeholder="受控端地址"
                onChange={e => this.handleUrlChanged(e.target.value)}
              />
              <FormControl.Feedback />
              <HelpBlock>
                请输入发送地址, 改地址指向应指向手机工具 App 的控制端口
              </HelpBlock>
            </FormGroup>
          </form>
        </Panel.Body>
      </Panel>
    );
  }
}
