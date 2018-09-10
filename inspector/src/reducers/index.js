import { combineReducers } from "redux";
import { createActions, handleActions, combineActions } from "redux-actions";

import { noticeReducer } from "./notice.js";
import { storageReducer } from "./storage.js";
import { uiReducer } from "./ui.js";

const rootReducer = combineReducers({
  app: combineReducers({
    notice: noticeReducer,
    storage: storageReducer,
    ui: uiReducer
  })
});

export default rootReducer;

export {
  NOTICE_ADD_SUCCESS,
  NOTICE_ADD_FAIL,
  NOTICE_REMOVE,
  NOTICE_CLEAR
} from "./notice.js";

export {
  /************* common ***************/
  STORAGE_CLEAR,
  STORAGE_LOAD_URL,
  STORAGE_CHANGE_URL,
  /************* msg ***************/
  STORAGE_LOAD_MSG_ALL_DATA,
  STORAGE_CHANGE_MSG_ID,
  STORAGE_CHANGE_MSG_BASE_INFO,
  STORAGE_CHANGE_MSG_DATA,
  /************* ui ***************/
  STORAGE_ADD_UI_RECORD,
  STORAGE_LOAD_UI_ALL_RECORD,
  STORAGE_CHANGE_UI_RECORD_INFO,
  STORAGE_LOAD_UI_RECORD,
  STORAGE_REMOVE_UI_RECORD,
  STORAGE_CHANGE_MSG_SEND_COUNT,
  STORAGE_CHANGE_MSG_SEND_INTERVAL
} from "./storage.js";

export {
  UI_CHANGE_VIEW_NODE_ROOT,
  UI_LOCK,
  UI_UNLOCK,
  UI_HOVER,
  UI_CHANGE_HIGHLIGHT_VIEW_INFO
} from "./ui.js";
