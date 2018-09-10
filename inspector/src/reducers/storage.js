import { combineReducers } from "redux";
import { createAction, handleActions, combineActions } from "redux-actions";
import path from "path";
import fs from "fs";
import Store from "electron-store";
import electron from "electron";
import dateformat from "dateformat";

const store = new Store();
const userDataPath = (electron.app || electron.remote.app).getPath("userData");
console.log("userDataPath: ", userDataPath);

function getRootPath(id) {
  return path.join(userDataPath, `ui_root_${id}.json`);
}
function getScreenCapPath(id) {
  return path.join(userDataPath, `ui_screen_cap_${id}.png`);
}

function saveRoot(id, data) {
  const outPath = getRootPath(id);
  try {
    fs.writeFileSync(outPath, JSON.stringify(data));
  } catch (e) {
    console.error(e);
  }
}

function saveScreenCap(id, data) {
  const outPath = getScreenCapPath(id);
  try {
    fs.writeFileSync(outPath, Buffer.from(data, "base64"));
  } catch (e) {
    console.error(e);
  }
}

function readRoot(id) {
  const inPath = getRootPath(id);
  try {
    return JSON.parse(fs.readFileSync(inPath).toString("utf8"));
  } catch (e) {
    console.error(e);
    return null;
  }
}

function readScreenCap(id) {
  const inPath = getScreenCapPath(id);
  try {
    return fs.readFileSync(inPath).toString("base64");
  } catch (e) {
    console.error(e);
    return null;
  }
}

function removeRoot(id) {
  const filePath = getRootPath(id);
  try {
    fs.unlinkSync(filePath);
  } catch (e) {
    console.error(e);
  }
}

function removeScreenCap(id) {
  const filePath = getScreenCapPath(id);
  try {
    fs.unlinkSync(filePath);
  } catch (e) {
    console.error(e);
  }
}

/*************** 通用 储存键值 ****************/
const SID_URL = "URL";

/*************** 模拟消息 储存键值 ****************/
const SID_MSG_ID = "MSG.ID";
const SID_MSG_LAST_BASE_INFO = "MSG.LAST_BASE_INFO";
const SID_MSG_BASE_INFO = bank => "MSG.BASE_INFO." + bank;
const SID_MSG_ALL_DATA = "MSG.ALL_DATA";
const SID_MSG_SEND_COUNT = "MSG.SEND_COUNT";
const SID_MSG_SEND_INTERVAL = "MSG.SEND_INTERVAL";

/*************** 界面嗅探 储存键值 ****************/
const SID_UI_ALL_RECORD_INFO = "UI.ALL_RECORD_INFO";
const SID_UI_LAST_RECORD_ID = "UI.LAST_RECORD_ID";

/*************** 通用 action ****************/
export const STORAGE_CLEAR = createAction("STORAGE_CLEAR");

export const STORAGE_LOAD_URL = createAction("STORAGE_LOAD_URL");
export const STORAGE_CHANGE_URL = createAction("STORAGE_CHANGE_URL", url => ({
  url
}));

/*************** 模拟消息 action ****************/
export const STORAGE_LOAD_MSG_ALL_DATA = createAction(
  "STORAGE_LOAD_MSG_ALL_DATA"
);

export const STORAGE_CHANGE_MSG_BASE_INFO = createAction(
  "STORAGE_CHANGE_MSG_BASE_INFO",
  baseInfo => ({
    baseInfo
  })
);

export const STORAGE_CHANGE_MSG_DATA = createAction(
  "STORAGE_CHANGE_MSG_DATA",
  data => ({
    data
  })
);

export const STORAGE_CHANGE_MSG_SEND_COUNT = createAction(
  "STORAGE_CHANGE_MSG_SEND_COUNT",
  sendCount => ({
    sendCount
  })
);

export const STORAGE_CHANGE_MSG_SEND_INTERVAL = createAction(
  "STORAGE_CHANGE_MSG_SEND_INTERVAL",
  sendInterval => ({
    sendInterval
  })
);

/*************** 界面嗅探 action ****************/
export const STORAGE_ADD_UI_RECORD = createAction(
  "STORAGE_ADD_UI_RECORD",
  data => ({
    data
  })
);
export const STORAGE_LOAD_UI_ALL_RECORD = createAction(
  "STORAGE_LOAD_UI_ALL_RECORD",
  data => ({
    data
  })
);
export const STORAGE_CHANGE_UI_RECORD_INFO = createAction(
  "STORAGE_CHANGE_UI_RECORD_INFO",
  recordInfo => ({
    recordInfo
  })
);
export const STORAGE_LOAD_UI_RECORD = createAction(
  "STORAGE_LOAD_UI_RECORD",
  id => ({
    id
  })
);
export const STORAGE_REMOVE_UI_RECORD = createAction(
  "STORAGE_REMOVE_UI_RECORD",
  id => ({
    id
  })
);

/*************** 通用 默认值 ****************/
export const DEFAULT_URL = "http://127.0.0.1:23300";

/*************** 模拟消息 默认值 ****************/
const DEFAULT_MSG_ID = -1000;
const DEFAULT_MSG_BASE_INFO = {
  appType: "alipay",
  aid: "",
  a_tradepass: "",
  a_loginpass: ""
};
const DEFAULT_FORM_DATA = {};
const DEFAULT_MSG_ALL_DATA = {};
const DEFAULT_SEND_COUNT = 2;
const DEFAULT_SEND_INTERVAL = 0.1;

/*************** 界面嗅探 默认值 ****************/
const DEFAULT_UI_ALL_RECORD_INFO = {};
const DEFAULT_UI_NOW_RECORD = {
  id: null,
  info: {
    id: null,
    now: null,
    bank: null,
    pkg: null,
    tag: null,
    now_activity: null,
    root_view_names: null,
    id_root: null,
    id_screencap: null
  },
  root: {},
  screencap: null
};

/*************** 完整的默认值 ****************/
const DEFAULT_STATE = {
  url: DEFAULT_URL,
  msg: {
    id: DEFAULT_MSG_ID,
    baseInfo: DEFAULT_MSG_BASE_INFO,
    allData: DEFAULT_MSG_ALL_DATA,
    sendCount: DEFAULT_SEND_COUNT,
    sendInterval: DEFAULT_SEND_INTERVAL
  },
  ui: {
    allRecordInfo: DEFAULT_UI_ALL_RECORD_INFO,
    nowRecord: DEFAULT_UI_NOW_RECORD
  }
};

/*************** 通用 缓存值 ****************/
var CACHE_URL = store.get(SID_URL, DEFAULT_URL);

/*************** 模拟消息 缓存值 ****************/
var CACHE_FORM_DATA = DEFAULT_FORM_DATA;
var CACHE_ID = DEFAULT_MSG_ID;

/*************** 界面嗅探 缓存值 ****************/
var CACHE_RECORD = DEFAULT_UI_NOW_RECORD;

import { genId, getNowFormData, deepCopy } from "./util.js";

export const storageReducer = handleActions(
  {
    /*************** 通用 reducer ****************/
    STORAGE_CLEAR: (state, action) => {
      store.clear();
      return DEFAULT_STATE;
    },

    STORAGE_LOAD_URL: (state, action) => {
      return { ...state, url: CACHE_URL };
    },

    STORAGE_CHANGE_URL: (state, { payload: { url } }) => {
      store.set(SID_URL, url);
      CACHE_URL = url;
      return { ...state, url };
    },

    /*************** 模拟消息 reducer ****************/
    STORAGE_LOAD_MSG_ALL_DATA: (state, action) => {
      const { msg } = state;
      const allData = store.get(SID_MSG_ALL_DATA, DEFAULT_MSG_ALL_DATA);
      const baseInfo = store.get(SID_MSG_LAST_BASE_INFO, DEFAULT_MSG_BASE_INFO);
      CACHE_ID = store.get(SID_MSG_ID, DEFAULT_MSG_ID);
      CACHE_FORM_DATA = getNowFormData(allData, CACHE_ID);
      const sendCount = store.get(SID_MSG_SEND_COUNT, DEFAULT_SEND_COUNT);
      const sendInterval = store.get(
        SID_MSG_SEND_INTERVAL,
        DEFAULT_SEND_INTERVAL
      );
      return {
        ...state,
        msg: {
          ...msg,
          id: CACHE_ID,
          formData: CACHE_FORM_DATA,
          baseInfo: baseInfo,
          allData: { ...allData },
          sendCount,
          sendInterval
        }
      };
    },

    STORAGE_CHANGE_MSG_BASE_INFO: (state, { payload: { baseInfo } }) => {
      const { msg } = state;
      if (msg.baseInfo.appType !== baseInfo.appType) {
        const changedBaseInfo = store.get(SID_MSG_BASE_INFO(baseInfo.appType));
        if (changedBaseInfo !== null && changedBaseInfo != undefined) {
          store.set(SID_MSG_LAST_BASE_INFO, changedBaseInfo);
          return {
            ...state,
            msg: { ...msg, baseInfo: deepCopy(changedBaseInfo) }
          };
        } else {
          const newBaseInfo = {
            ...DEFAULT_MSG_BASE_INFO,
            appType: baseInfo.appType
          };
          store.set(SID_MSG_LAST_BASE_INFO, newBaseInfo);
          return {
            ...state,
            msg: { ...msg, baseInfo: newBaseInfo }
          };
        }
      }
      store.set(SID_MSG_LAST_BASE_INFO, baseInfo);
      store.set(SID_MSG_BASE_INFO(baseInfo.appType), baseInfo);
      return { ...state, msg: { ...msg, baseInfo: deepCopy(baseInfo) } };
    },

    STORAGE_CHANGE_MSG_DATA: (state, { payload: { data } }) => {
      const { msg } = state;
      const allData = deepCopy(
        store.get(SID_MSG_ALL_DATA, DEFAULT_MSG_ALL_DATA)
      );
      if (CACHE_ID == data.id) {
        allData[data.id] = deepCopy(data);
        CACHE_FORM_DATA = data;
      } else {
        CACHE_ID = data.id;
        CACHE_FORM_DATA = getNowFormData(allData, CACHE_ID);
      }
      store.set(SID_MSG_ID, CACHE_ID);
      store.set(SID_MSG_ALL_DATA, allData);
      return {
        ...state,
        msg: {
          ...msg,
          id: data.id,
          formData: CACHE_FORM_DATA,
          allData: allData
        }
      };
    },
    STORAGE_CHANGE_MSG_SEND_COUNT: (state, { payload: { sendCount } }) => {
      const { msg } = state;
      store.set(SID_MSG_SEND_COUNT, sendCount);
      return {
        ...state,
        msg: {
          ...msg,
          sendCount
        }
      };
    },
    STORAGE_CHANGE_MSG_SEND_INTERVAL: (
      state,
      { payload: { sendInterval } }
    ) => {
      const { msg } = state;
      store.set(SID_MSG_SEND_INTERVAL, sendInterval);
      return {
        ...state,
        msg: {
          ...msg,
          sendInterval
        }
      };
    },
    /*************** 界面嗅探 reducer ****************/
    STORAGE_ADD_UI_RECORD: (state, action) => {
      const {
        payload: {
          data: { info, root, screencap }
        }
      } = action;
      const {
        ui: { allRecordInfo }
      } = state;
      const id = genId();
      const id_root = genId();
      const id_screencap = genId();

      const newRecordInfo = { ...info, id, id_root, id_screencap };
      newRecordInfo.now = dateformat(new Date(), "yyyy-mm-dd HH:MM:ss");

      allRecordInfo[id] = newRecordInfo;

      CACHE_RECORD = {
        id: id,
        info: newRecordInfo,
        root,
        screencap
      };
      store.set(SID_UI_LAST_RECORD_ID, id);
      store.set(SID_UI_ALL_RECORD_INFO, allRecordInfo);
      saveRoot(id_root, root);
      saveScreenCap(id_screencap, screencap);
      return { ...state, ui: { allRecordInfo, nowRecord: CACHE_RECORD } };
    },
    STORAGE_LOAD_UI_ALL_RECORD: (state, action) => {
      const allRecordInfo = store.get(
        SID_UI_ALL_RECORD_INFO,
        DEFAULT_UI_ALL_RECORD_INFO
      );
      const allIds = Object.keys(allRecordInfo);
      if (allIds && allIds.length > 0) {
        const lastId = store.get(SID_UI_LAST_RECORD_ID, "");
        var id = lastId;
        var info = allRecordInfo[id];
        if (info === null || info === undefined) {
          id = allIds[allIds.length - 1];
          info = allRecordInfo[id];
        }
        CACHE_RECORD = {
          id: id,
          info: info,
          root: readRoot(info.id_root),
          screencap: readScreenCap(info.id_screencap)
        };
      }
      return { ...state, ui: { allRecordInfo, nowRecord: CACHE_RECORD } };
    },
    STORAGE_CHANGE_UI_RECORD_INFO: (state, action) => {
      const {
        payload: { recordInfo }
      } = action;
      const {
        ui: { allRecordInfo }
      } = state;
      const id = recordInfo.id;
      const newRecordInfo = { ...allRecordInfo[id], ...recordInfo };
      allRecordInfo[id] = newRecordInfo;
      store.set(SID_UI_ALL_RECORD_INFO, allRecordInfo);
      if (CACHE_RECORD.id == recordInfo.id) {
        CACHE_RECORD.info = newRecordInfo;
      }
      return { ...state, ui: { allRecordInfo, nowRecord: CACHE_RECORD } };
    },
    STORAGE_LOAD_UI_RECORD: (state, action) => {
      const {
        payload: { id }
      } = action;
      const {
        ui: { allRecordInfo }
      } = state;
      const info = allRecordInfo[id];
      if (info == undefined) {
        store.set(SID_UI_LAST_RECORD_ID, null);
        CACHE_RECORD = { ...DEFAULT_UI_NOW_RECORD };
        return { ...state, ui: { ...state.ui, nowRecord: CACHE_RECORD } };
      }
      store.set(SID_UI_LAST_RECORD_ID, id);
      CACHE_RECORD = {
        id: id,
        info: info,
        root: readRoot(info.id_root),
        screencap: readScreenCap(info.id_screencap)
      };
      return { ...state, ui: { ...state.ui, nowRecord: CACHE_RECORD } };
    },
    STORAGE_REMOVE_UI_RECORD: (state, action) => {
      const {
        payload: { id }
      } = action;
      const {
        ui: { allRecordInfo, nowRecord }
      } = state;
      delete allRecordInfo[id];
      store.set(SID_UI_ALL_RECORD_INFO, allRecordInfo);
      removeRoot(nowRecord.info.id_root);
      removeScreenCap(nowRecord.info.id_screencap);

      if (nowRecord.id == id) {
        const allIds = Object.keys(allRecordInfo);
        if (allIds && allIds.length > 0) {
          const lastId = allIds[allIds.length - 1];
          const info = allRecordInfo[lastId];
          store.set(SID_UI_LAST_RECORD_ID, lastId);
          CACHE_RECORD = {
            id: lastId,
            info: info,
            root: readRoot(info.id_root),
            screencap: readScreenCap(info.id_screencap)
          };
        } else {
          store.set(SID_UI_LAST_RECORD_ID, "");
          CACHE_RECORD = DEFAULT_UI_NOW_RECORD;
        }
      }
      return { ...state, ui: { allRecordInfo, nowRecord: CACHE_RECORD } };
    }
  },
  DEFAULT_STATE
);
