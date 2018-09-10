import React, { Component } from "react";
import PropTypes from "prop-types";
import {
  PageHeader,
  Panel,
  ListGroup,
  ListGroupItem,
  Glyphicon
} from "react-bootstrap";

import style from "./style.css";

const keys = [
  "pkg",
  "cls",
  "res",
  "text",
  "hint",
  "desc",
  "tag",
  "index",
  "bounds_on_screen",
  "drawing_rect",
  "collection",
  "collection_item",
  "is_visible",
  "is_enable",
  "is_clickable",
  "is_long_clickable",
  "is_focusable",
  "is_focused",
  "is_selected",
  "is_context_clickable",
  "is_scrollable",
  "is_checked",
  "is_dismissable",
  "is_editable",
  "is_multiLine",
  "is_password"
];

class FakeDiv extends Component {
  render() {
    return this.props.children;
  }
}

class HeadRow extends Component {
  render() {
    return <tr className={style.headRow}>{this.props.children}</tr>;
  }
}

class HeadCol extends Component {
  render() {
    return <td className={style.headCol}>{this.props.children}</td>;
  }
}

class BodyRow extends Component {
  render() {
    return <tr className={style.bodyRow}>{this.props.children}</tr>;
  }
}

class BodyCol extends Component {
  render() {
    const self = this;
    const type = typeof this.props.children;
    if (type === "string" || type === "number") {
      return (
        <td
          className={style.bodyCol}
          onClick={() => {
            self.textarea.select();
          }}
        >
          <textarea
            ref={ref => (self.textarea = ref)}
            cols={("" + this.props.children).length}
            readOnly
            className={style.bodyColTextarea}
            value={this.props.children}
          />
        </td>
      );
    } else {
      return <td className={style.bodyCol}>{this.props.children}</td>;
    }
  }
}

export default class ViewInfo extends Component {
  static defaultProps = {
    info: null
  };

  renderPair(key, value) {
    if (value === undefined) {
      return null;
    }
    if (value.top !== undefined) {
      return this.renderPairRect(key, value);
    } else if (value.row_count !== undefined) {
      return this.renderPairCollection(key, value);
    } else if (value.row_index !== undefined) {
      return this.renderPairCollectionItem(key, value);
    } else if (value === true) {
      return this.renderPairTrue(key);
    } else if (value === false) {
      return this.renderPairFalse(key);
    } else {
      return (
        <BodyRow key={key}>
          <BodyCol>{key}</BodyCol>
          <BodyCol>{value}</BodyCol>
        </BodyRow>
      );
    }
  }

  renderPairTrue(key) {
    return (
      <BodyRow key={key}>
        <BodyCol>{key}</BodyCol>
        <BodyCol>
          <Glyphicon glyph="ok" style={{ color: "green" }} />
        </BodyCol>
      </BodyRow>
    );
  }

  renderPairFalse(key) {
    return (
      <BodyRow key={key}>
        <BodyCol>{key}</BodyCol>
        <BodyCol>
          <Glyphicon glyph="remove" style={{ color: "red" }} />
        </BodyCol>
      </BodyRow>
    );
  }

  renderPairRect(key, value) {
    var rectStr = "";
    var x = value.left;
    var y = value.top;
    var w = value.right - value.left;
    var h = value.bottom - value.top;
    rectStr += "[" + x + "," + y + "]";
    rectStr += "[" + w + "," + h + "]";
    return (
      <BodyRow key={key}>
        <BodyCol>{key}</BodyCol>
        <BodyCol>{rectStr}</BodyCol>
      </BodyRow>
    );
  }

  renderPairCollection(key, value) {
    var ret = (
      <BodyRow key={key}>
        <BodyCol>{key}</BodyCol>
        <BodyCol>
          {"[" + value.row_count + ", " + value.column_count + "]"}
        </BodyCol>
      </BodyRow>
    );

    if (value.is_hierarchical === true) {
      ret = (
        <FakeDiv key={key}>
          {ret}
          {this.renderPairTrue(key + ":is_hierarchical")}
        </FakeDiv>
      );
    } else if (value.is_hierarchical === false) {
      ret = (
        <FakeDiv key={key}>
          {ret}
          {this.renderPairFalse(key + ":is_hierarchical")}
        </FakeDiv>
      );
    }
    ret = (
      <FakeDiv key={key}>
        {ret}
        <BodyRow>
          <BodyCol>{key + ":selection_mode"}</BodyCol>
          <BodyCol>{value.selection_mode}</BodyCol>
        </BodyRow>
      </FakeDiv>
    );
    return ret;
  }

  renderPairCollectionItem(key, value) {
    var ret = (
      <FakeDiv key={key}>
        <BodyRow>
          <BodyCol>{key}</BodyCol>
          <BodyCol>
            {"[" +
              value.row_index +
              ", " +
              value.column_index +
              "] [" +
              value.row_span +
              ", " +
              value.column_span +
              "]"}
          </BodyCol>
        </BodyRow>
      </FakeDiv>
    );
    if (value.is_heading === true) {
      ret = (
        <FakeDiv key={key}>
          {ret}
          {this.renderPairTrue(key + ":is_heading")}
        </FakeDiv>
      );
    } else if (value.is_heading === false) {
      ret = (
        <FakeDiv key={key}>
          {ret}
          {this.renderPairFalse(key + ":is_heading")}
        </FakeDiv>
      );
    }
    if (value.is_selected === true) {
      ret = (
        <FakeDiv key={key}>
          {ret}
          {this.renderPairTrue(key + ":is_selected")}
        </FakeDiv>
      );
    } else if (value.is_selected === false) {
      ret = (
        <FakeDiv key={key}>
          {ret}
          {this.renderPairFalse(key + ":is_selected")}
        </FakeDiv>
      );
    }
    return ret;
  }

  renderPairs() {
    const { info } = this.props;
    if (info === null || info === undefined) {
      return null;
    }

    const self = this;
    const key_count = Object.keys(info).length;
    if (key_count <= 0) return null;

    return (
      <FakeDiv>
        {keys.map((key, i) => self.renderPair(key, info[key], i))}
      </FakeDiv>
    );
  }

  render() {
    return (
      <div className={style.viewInfo}>
        <table className={style.table}>
          <thead>
            <HeadRow>
              <HeadCol>字段名</HeadCol>
              <HeadCol>值</HeadCol>
            </HeadRow>
          </thead>
          <tbody>{this.renderPairs()}</tbody>
        </table>
      </div>
    );
  }
}
