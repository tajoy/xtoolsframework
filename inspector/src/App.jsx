import React, { Component } from "react";
import { connect } from "react-redux";
import { Route, Switch } from "react-router-dom";

import "bootstrap/dist/css/bootstrap.css";
import "bootstrap/dist/css/bootstrap-theme.css";

import Page from "@components/Page";

import HomePage from "@pages/HomePage";
import UiPage from "@pages/UiPage";
import MsgPage from "@pages/MsgPage";

export default class App extends Component {
  render() {
    return (
      <Switch>
        <Route exact path="/">
          <Page>
            <HomePage />
          </Page>
        </Route>
        <Route path="/ui">
          <Page>
            <UiPage />
          </Page>
        </Route>
        <Route path="/msg">
          <Page>
            <MsgPage />
          </Page>
        </Route>
      </Switch>
    );
  }
}
