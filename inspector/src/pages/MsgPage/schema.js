import React, { Component } from "react";
import dateFormat from "dateformat";
import flatpickr from "flatpickr";
import Flatpickr from "react-flatpickr";
import "flatpickr/dist/flatpickr.css";
import {
  ButtonToolbar,
  Button,
  ToggleButtonGroup,
  ToggleButton,
  Tooltip,
  OverlayTrigger
} from "react-bootstrap";
import Toggle from "react-bootstrap-toggle";
import "react-bootstrap-toggle/dist/bootstrap2-toggle.css";

var event_name_map = {
  [-1000]: "EVENT_TEST",
};
const event_id_map = (function(json) {
  var ret = {};
  for (var key in json) {
    ret[json[key]] = Math.floor(key * 1);
  }
  return ret;
})(event_name_map);

const ids = Object.keys(event_name_map).map(function(id) {
  return Math.floor(id * 1);
});
const enum_titles = ids.map(function(id) {
  return id + " " + event_name_map[id];
});

const event_defines = {
  EVENT_TEST: {
    title: "测试事件",
    type: "object",
    properties: {}
  },
};

function convertDefines(defines) {
  var schemas = {};
  const keys = Object.keys(defines);
  if (keys && keys.length <= 0) return schemas;

  for (const key in defines) {
    const def = defines[key];
    if (def === undefined) {
      continue;
    }
    schemas[event_id_map[key]] = {
      // title: "消息对象",
      type: "object",
      required: [],
      properties: {
        id: {
          title: "事件Id",
          type: "number",
          default: -1000,
          enum: ids,
          enumNames: enum_titles
        },
        data: def
      },
      required: ["id", "data"]
    };
  }
  return schemas;
}

export const schemaDefines = convertDefines(event_defines);

flatpickr.localize({
  weekdays: {
    shorthand: ["周日", "周一", "周二", "周三", "周四", "周五", "周六"],
    longhand: [
      "星期日",
      "星期一",
      "星期二",
      "星期三",
      "星期四",
      "星期五",
      "星期六"
    ]
  },
  months: {
    shorthand: [
      "一月",
      "二月",
      "三月",
      "四月",
      "五月",
      "六月",
      "七月",
      "八月",
      "九月",
      "十月",
      "十一月",
      "十二月"
    ],
    longhand: [
      "一月",
      "二月",
      "三月",
      "四月",
      "五月",
      "六月",
      "七月",
      "八月",
      "九月",
      "十月",
      "十一月",
      "十二月"
    ]
  },
  rangeSeparator: " 至 ",
  weekAbbreviation: "周",
  scrollTitle: "滚动切换",
  toggleTitle: "点击切换 12/24 小时时制"
});
const DateTimeWidget = props => {
  const options = {
    defaultDate: "today",
    time_24hr: true,
    enableTime: true,
    enableSeconds: true,
    dateFormat: "Y-m-d H:i:S",
    maxDate: "today",
    locale: {
      firstDayOfWeek: 1 // start week on Monday
    }
  };
  const tooltip = (
    <Tooltip id={"tooltip-" + props.id}>
      <strong>点击选择日期时间</strong>
    </Tooltip>
  );
  return (
    <OverlayTrigger placement="right" overlay={tooltip}>
      <Flatpickr
        disabled={props.disabled || props.readonly}
        className="flatpickr-input form-control"
        options={options}
        value={props.value}
        onChange={date => {
          props.onChange(dateFormat(date, "yyyy-mm-dd HH:MM:ss"));
        }}
      />
    </OverlayTrigger>
  );
};

export const uiSchema = {
  data: {
    "ui:options": {},
    amount: {
      // items: {
      //   "ui:widget": DateTimeWidget
      // }
    }
  }
};

export const schemaBaseInfo = {
  // title: "基本信息",
  type: "object",
  required: [],
  properties: {
  }
};

export const uiSchemaBaseInfo = {
  testing: {
    "ui:widget": props => {
      return (
        <Toggle
          recalculateOnResize
          active={props.value}
          disabled={props.disabled || props.readonly}
          size="lg"
          onClick={() => props.onChange(!props.value)}
          on="开启"
          onstyle="success"
          offstyle="danger"
          off="关闭"
        />
      );
    }
  }
};
