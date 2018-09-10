import React, { Component } from "react";
import PropTypes from "prop-types";
import {
  PageHeader,
  Panel,
  ListGroup,
  ListGroupItem,
  Table,
  Button,
  Glyphicon,
  OverlayTrigger,
  Tooltip,
  Label,
  FormGroup,
  FormControl
} from "react-bootstrap";

function isEmpty(o) {
  return o === null || o === undefined || o === "";
}

export default class RecordSelector extends Component {
  static propTypes = {
    nowId: PropTypes.string,
    all: PropTypes.object,
    onSelect: PropTypes.func,
    onDelete: PropTypes.func,
    onNewTag: PropTypes.func
  };

  static defaultProps = {
    nowId: null,
    all: null,
    onSelect: id => {},
    onDelete: id => {},
    onNewTag: (id, newTag) => {}
  };

  constructor(props) {
    super(props);
    this.state = {
      editingId: null
    };
  }

  onRowClick = (e, id) => {
    const { onSelect } = this.props;
    // console.log({ ...e });
    if ("INPUT" === e.target.nodeName) {
      return;
    }
    onSelect && onSelect(id);
  };

  onRemoveClick = id => {
    const { onDelete } = this.props;
    onDelete && onDelete(id);
  };

  onTagClick = id => {
    this.setState({ editingId: id });
  };

  onTagChange = (e, id) => {
    const { onNewTag } = this.props;
    const tag = this.tagInput.value;
    // console.log({ ...e }, id, tag);

    onNewTag && onNewTag(id, tag);
    this.setState({ editingId: null });
  };

  render() {
    const { editingId } = this.state;
    const { nowId, all } = this.props;
    const ids = Object.keys(all);
    const self = this;

    return (
      <Table striped bordered condensed hover>
        <thead>
          <tr>
            <th>时间</th>
            <th>包名</th>
            <th>标签</th>
            <th>{}</th>
          </tr>
        </thead>
        <tbody>
          {ids.map(id => (
            <tr
              key={id}
              rid={id}
              style={
                id !== nowId ? {} : { backgroundColor: "rgba(66, 66, 66, 0.1)" }
              }
              onClick={e => self.onRowClick(e, id)}
            >
              <td>{all[id].now}</td>
              <td>{all[id].pkg}</td>
              {editingId === id ? (
                <td>
                  <form onSubmit={e => self.onTagChange(e, id)}>
                    <FormGroup
                      controlId="tag"
                      onBlur={e => self.onTagChange(e, id)}
                    >
                      <FormControl
                        type="text"
                        autoFocus
                        inputRef={ref => {
                          self.tagInput = ref;
                          if (ref) {
                            var tag = all[id].tag;
                            tag = isEmpty(tag) ? "" : tag;
                            ref.value = tag;
                            ref.select();
                          }
                        }}
                      />
                    </FormGroup>
                  </form>
                </td>
              ) : (
                <OverlayTrigger
                  placement="bottom"
                  overlay={<Tooltip id="tooltip-tag">点击编辑</Tooltip>}
                >
                  <td onClick={() => self.onTagClick(id)}>
                    {isEmpty(all[id].tag) ? (
                      <Label>[未定义]</Label>
                    ) : (
                      all[id].tag
                    )}
                  </td>
                </OverlayTrigger>
              )}
              <td>
                {id !== nowId ? (
                  ""
                ) : (
                  <OverlayTrigger
                    placement="right"
                    overlay={<Tooltip id="tooltip-delete">点击删除</Tooltip>}
                  >
                    <Button
                      bsSize="xs"
                      bsStyle="danger"
                      onClick={() => self.onRemoveClick(id)}
                    >
                      <Glyphicon glyph="trash" />
                    </Button>
                  </OverlayTrigger>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </Table>
    );
  }
}
