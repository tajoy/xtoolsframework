import { combineReducers } from "redux";
import { createAction, handleActions, combineActions } from "redux-actions";
import dateformat from "dateformat";

var msg_id_source = 0;

export const NOTICE_ADD_SUCCESS = createAction("NOTICE_ADD_SUCCESS", msg => ({
  id: msg_id_source++,
  text: msg,
  bsStyle: "success"
}));

export const NOTICE_ADD_FAIL = createAction("NOTICE_ADD_FAIL", msg => ({
  id: msg_id_source++,
  text: msg,
  bsStyle: "danger"
}));

export const NOTICE_REMOVE = createAction("NOTICE_REMOVE", id => ({
  id: id
}));

export const NOTICE_CLEAR = createAction("NOTICE_CLEAR");

function fmtMsg(payload, no) {
  const now = dateformat(new Date(), "yyyy-mm-dd HH:MM:ss");
  payload.text = `[${now}] ${payload.text}`;
  payload.no = (no === null || no === undefined || isNaN(no)) ? 0 : no;
  return payload;
}

export const noticeReducer = handleActions(
  {
    NOTICE_ADD_SUCCESS: (state, action) => {
      const no = state.no + 1;
      return { items: [...state.items, fmtMsg(action.payload, no)], no };
    },
    NOTICE_ADD_FAIL: (state, action) => {
      const no = state.no + 1;
      return { items: [...state.items, fmtMsg(action.payload, no)], no };
    },
    NOTICE_REMOVE: (state, action) => {
      var found = null;
      const items = state.items;
      const id = action.payload.id;
      for (const index in items) {
        const item = items[index];
        if (id == item.id) {
          found = index;
          break;
        }
      }
      if (found !== null) {
        items.splice(found, 1);
        return { items: [...items] };
      } else {
        return state;
      }
    },
    NOTICE_CLEAR: (state, action) => {
      return { items: [] };
    }
  },
  { items: [], no: 0 }
);
