import "babel-polyfill";
require("es6-promise").polyfill();

import React from "react";
import ErrorBoundary from "react-error-boundary";
import { render } from "react-dom";
import { combineReducers, createStore, applyMiddleware, compose } from "redux";
import { connect, Provider } from "react-redux";
import {
  connectRouter,
  routerMiddleware,
  ConnectedRouter
} from "connected-react-router";
import { MemoryRouter, HashRouter } from "react-router-dom";

import thunk from "redux-thunk";
import { createLogger } from "redux-logger";
import {
  createBrowserHistory,
  createHashHistory,
  createMemoryHistory
} from "history";

import createElectronHistory from "./history/createElectronHistory.js";

// import installExtension, {
//   REACT_DEVELOPER_TOOLS,
//   JQUERY_DEBUGGER,
//   REDUX_DEVTOOLS,
//   REACT_PERF
// } from "electron-devtools-installer";

// [
//   REACT_DEVELOPER_TOOLS,
//   REACT_PERF,
//   REDUX_DEVTOOLS,
//   JQUERY_DEBUGGER
// ].forEach(ext =>
//   installExtension(ext)
//     .then(name => console.log(`Added Extension:  ${name}`))
//     .catch(err => console.log("An error occurred: ", err))
// );

import App from "./App";

import "./global.css";

import rootReducer from "./reducers/";

const logger = createLogger({
  predicate: (getState, action) => {
    switch (action.type) {
      case "UI_HOVER":
        return false;
    }
    return true;
  },
  collapsed: true,
  duration: true,
  diff: true,
});

const history = createElectronHistory();
const store = createStore(
  connectRouter(history)(rootReducer),
  compose(applyMiddleware(routerMiddleware(history), thunk, logger))
);

// history.listen(location => console.log(location));
const onError = (error, componentStack) => {
  console.error(error, componentStack);
};

const FallbackComponent = ({ componentStack, error }) => (
  <div style={{ margin: 32, padding: 32, color: "#F00" }}>
    <p>ERROR:</p>
    <pre style={{ backgroundColor: "red", color: "#FFF" }}>{"" + error}</pre>
    <p>COMPONENT STACK:</p>
    <pre style={{ backgroundColor: "red", color: "#FFF" }}>
      {componentStack}
    </pre>
  </div>
);

render(
  <ErrorBoundary onError={onError} FallbackComponent={FallbackComponent}>
    <Provider store={store}>
      <ConnectedRouter history={history}>
        <App />
      </ConnectedRouter>
    </Provider>
  </ErrorBoundary>,
  document.getElementById("app")
);
