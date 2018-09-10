import storage from "./storage.js";
import { combineReducers } from "redux";
import { createAction, handleActions, combineActions } from "redux-actions";

import { genId, deepCopy } from "./util.js";

export const UI_CHANGE_VIEW_NODE_ROOT = createAction(
  "UI_CHANGE_VIEW_NODE_ROOT",
  root => ({
    root
  })
);
export const UI_LOCK = createAction("UI_LOCK");
export const UI_UNLOCK = createAction("UI_UNLOCK");
export const UI_HOVER = createAction("UI_HOVER", (x, y, color) => ({
  x,
  y,
  color
}));
export const UI_CHANGE_HIGHLIGHT_VIEW_INFO = createAction(
  "UI_CHANGE_HIGHLIGHT_VIEW_INFO",
  info => ({
    info
  })
);

function createAreaByInfo(info) {
  if (!info) return null;
  const left = info.bounds_on_screen.left;
  const top = info.bounds_on_screen.top;
  const right = info.bounds_on_screen.right;
  const bottom = info.bounds_on_screen.bottom;
  var area = {
    left: left,
    top: top,
    right: right,
    bottom: bottom,
    width: right - left,
    height: bottom - top
  };
  area.info = info;
  // area.id = info._id;
  return area;
}

const CACHE_AREAS = [];

export const uiReducer = handleActions(
  {
    UI_CHANGE_VIEW_NODE_ROOT: (state, action) => {
      const {
        payload: { root }
      } = action;
      const areas = [];
      if (root !== null) {
        var nodeStack = [];
        var infoStack = [];
        nodeStack.push(root);
        while (nodeStack.length > 0) {
          const node = nodeStack.pop();
          if (!node) break;
          const info = node.info;
          if (info._id === undefined) {
            info._id = genId();
          }
          infoStack.push(info);
          const children = node.children;
          if (children && children.length > 0) {
            for (
              let index = children.length - 1;
              children && index >= 0;
              index--
            ) {
              const childNode = children[index];
              nodeStack.push(childNode);
            }
          }
        }
        infoStack = infoStack.reverse();
        for (let i = 0; i < infoStack.length; i++) {
          const info = infoStack[i];
          if (!info.is_visible) {
            continue;
          }
          areas.push(createAreaByInfo(info));
        }
      }
      return { ...state, areas, root };
    },
    UI_LOCK: (state, action) => {
      return { ...state, isLock: true };
    },
    UI_UNLOCK: (state, action) => {
      return { ...state, isLock: false };
    },
    UI_HOVER: (state, action) => {
      const {
        payload: { x, y, color }
      } = action;
      const { areas, isLock } = state;
      if (isLock) {
        return state;
      }

      for (let i = 0; i < areas.length; i++) {
        const area = areas[i];
        if (area.left > x) {
          continue;
        }
        if (area.right < x) {
          continue;
        }
        if (area.top > y) {
          continue;
        }
        if (area.bottom < y) {
          continue;
        }
        return { ...state, highlightViewInfo: area.info, isNeedFocusInThree: true };
      }
      return state;
    },
    UI_CHANGE_HIGHLIGHT_VIEW_INFO: (state, action) => {
      const {
        payload: { info }
      } = action;
      const { isLock } = state;
      if (isLock) {
        return state;
      }
      return { ...state, highlightViewInfo: info, isNeedFocusInThree: false };
    }
  },
  {
    areas: [],
    isLock: false,
    highlightViewInfo: null,
    root: null,
    isNeedFocusInThree: false,
  }
);
