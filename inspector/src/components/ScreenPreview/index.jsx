import React, { Component } from "react";
import PropTypes from "prop-types";
import DocumentEvents from "react-document-events";
import style from "./style.css";

const INIT_NUMBER = 10;
const HIDDEN = "hidden";
const VISIBLE = "visible";
const GRID_COLOR = "lightgray";
const CENTERGRID_COLOR = "black";
const DEFAULT_COLOR = "#fff";

const transform2rgba = arr => {
  arr[3] = parseFloat(arr[3] / 255);
  return `rgba(${arr.join(", ")})`;
};

const rgb2hex_a = rgb => {
  const result = rgb.match(
    /^rgba?[\s+]?\([\s+]?(\d+)[\s+]?,[\s+]?(\d+)[\s+]?,[\s+]?(\d+)[\s+]?,[\s+]?(\d+(\.\d)?)[\s+]?/i
  );
  if (result && result.length >= 4) {
    const r = parseInt(result[1], 10);
    const rStr = ("0" + r.toString(16)).slice(-2);

    const g = parseInt(result[2], 10);
    const gStr = ("0" + g.toString(16)).slice(-2);

    const b = parseInt(result[3], 10);
    const bStr = ("0" + b.toString(16)).slice(-2);
    return {
      hex: ("#" + rStr + gStr + bStr).toUpperCase(),
      o: +result[4]
    };
  } else {
    return ("" + rgb).toUpperCase();
  }
};

const drawGrid = (context, color, stepx, stepy) => {
  context.strokeStyle = color;
  context.lineWidth = 0.5;

  for (let i = stepx + 0.5; i < context.canvas.width; i += stepx) {
    context.beginPath();
    context.moveTo(i, 0);
    context.lineTo(i, context.canvas.height);
    context.stroke();
  }

  for (let i = stepy + 0.5; i < context.canvas.height; i += stepy) {
    context.beginPath();
    context.moveTo(0, i);
    context.lineTo(context.canvas.width, i);
    context.stroke();
  }
};

const drawImageSmoothingEnable = (context, enable) => {
  context.mozImageSmoothingEnabled = enable;
  context.webkitImageSmoothingEnabled = enable;
  context.msImageSmoothingEnabled = enable;
  context.imageSmoothingEnabled = enable;
};

const drawCenterRect = (context, color, x, y, width, height) => {
  context.strokeStyle = color;
  context.lineWidth = 1;
  context.strokeRect(x, y, width, height);
  // context.beginPath()
  // context.strokeStyle = color
  // context.lineWidth = 1
  // context.moveTo(x, y)
  // context.lineTo(x + width, y)
  // context.lineTo(x + width, y + height)
  // context.lineTo(x, y + height)
  // context.lineTo(x, y)
  // context.stroke()
  // context.beginPath()
  // context.lineWidth = 2
  // context.strokeStyle = 'rgba(255,255,255,0.8)'
  // context.moveTo(x - 2, y - 2)
  // context.lineTo(x + width + 2, y - 2)
  // context.lineTo(x + width + 2, y + 2 + height)
  // context.lineTo(x - 2, y + height + 2)
  // context.lineTo(x - 2, y - 2)
  // context.stroke()
};

export default class ScreenPreview extends Component {
  static propTypes = {
    src: PropTypes.string,
    glassHeight: PropTypes.number,
    glassWidth: PropTypes.number,
    scale: PropTypes.number,
    isLock: PropTypes.bool,
    rect: PropTypes.object,
    pickColor: PropTypes.func,
    onHover: PropTypes.func,
    onClick: PropTypes.func,
    containerRef: PropTypes.func
  };
  static defaultProps = {
    src: "",
    isLock: false,
    rect: { x: 0, y: 0, width: 0, height: 0 },
    glassWidth: 160,
    glassHeight: 160,
    scale: 1,
    onHover: (x, y, color) => console.log("onHover", x, y, color),
    onClick: (x, y, color) => console.log("onClick", x, y, color),
    containerRef: ref => {}
  };

  constructor(props) {
    super(props);
    this.state = {
      x: 0,
      y: 0,
      isHoldCtrl: false,
      isShouldShowGlass: false,
      glassLeft: 0,
      glassTop: 0,
      color: DEFAULT_COLOR
    };
  }

  updateRect() {
    const img = this.image;
    var { width, height, clientWidth, clientHeight } = img;
    const { isLock, rect } = this.props;

    this.rectCanvas.style.width = clientWidth + "px";
    this.rectCanvas.style.height = clientHeight + "px";
    this.rectCanvas.width = clientWidth;
    this.rectCanvas.height = clientHeight;
    this.rectCtx.clearRect(0, 0, clientWidth, clientHeight);

    this.rectCanvas.style.width = clientWidth + "px";
    this.rectCanvas.style.height = clientHeight + "px";
    this.rectCanvas.width = clientWidth;
    this.rectCanvas.height = clientHeight;
    this.rectCtx.clearRect(0, 0, clientWidth, clientHeight);

    const imgScaleX = clientWidth / img.naturalWidth;
    const imgScaleY = clientHeight / img.naturalHeight;

    const rectX = Math.floor(rect.x * imgScaleX);
    const rectY = Math.floor(rect.y * imgScaleY);
    const rectWidth = Math.floor(rect.width * imgScaleX);
    const rectHeight = Math.floor(rect.height * imgScaleY);

    this.rectCtx.lineWidth = 0.2;
    this.rectCtx.setLineDash([5]);
    this.rectCtx.strokeStyle = "#000";
    this.rectCtx.strokeRect(1, 1, clientWidth - 2, clientHeight - 2);

    this.rectCtx.lineWidth = 1.0;
    if (isLock) {
      this.rectCtx.setLineDash([]);
      this.rectCtx.strokeStyle = "#F00";
    } else {
      this.rectCtx.setLineDash([6]);
      this.rectCtx.strokeStyle = "#F22";
    }
    this.rectCtx.strokeRect(rectX, rectY, rectWidth, rectHeight);
  }

  onLoadImg = e => {
    const img = e.target;
    var { width, height, clientWidth, clientHeight } = img;
    this.imageCanvas.style.width = img.naturalWidth + "px";
    this.imageCanvas.style.height = img.naturalHeight + "px";
    this.imageCanvas.width = img.naturalWidth;
    this.imageCanvas.height = img.naturalHeight;
    this.imageCtx.drawImage(img, 0, 0, img.naturalWidth, img.naturalHeight);
    this.updateRect();
  };

  imageContainerRef = ref => {
    this.image = ref;
  };
  imageCanvasRef = ref => (this.imageCanvas = ref);
  rectCanvasRef = ref => (this.rectCanvas = ref);
  glassCanvasRef = ref => (this.glassCanvas = ref);

  onWindowResize = e => {
    this.forceUpdate();
    this.updateRect();
  };

  componentDidMount() {
    this.imageCtx = this.imageCanvas.getContext("2d");
    this.rectCtx = this.rectCanvas.getContext("2d");
    this.glassCtx = this.glassCanvas.getContext("2d");
    window.addEventListener("resize", this.onWindowResize);
  }
  
  componentDidUpdate() {
    this.updateRect();
  }

  componentWillUnmount() {
    window.removeEventListener("resize", this.onWindowResize);
  }

  calculateCenterPoint = e => {
    const { left, top } = this.image.getBoundingClientRect();
    const centerX = Math.floor(e.clientX - left);
    const centerY = Math.floor(e.clientY - top);

    const imgScaleX = this.image.width / this.image.naturalWidth;
    const imgScaleY = this.image.height / this.image.naturalHeight;

    const imgX = Math.floor(centerX / imgScaleX);
    const imgY = Math.floor(centerY / imgScaleY);
    return {
      centerX,
      centerY,
      imgX,
      imgY
    };
  };

  handleMove = e => {
    this.setState({ isShouldShowGlass: true });

    const point = this.calculateCenterPoint(e);
    const { onHover, isLock } = this.props;
    const { glassHeight, glassWidth, scale } = this.props;
    const { centerX, centerY, imgX, imgY } = point;
    const glassLeft = Math.floor(centerX - glassWidth / 2);
    const glassTop = Math.floor(centerY - glassHeight / 2);
    const { data } = this.imageCtx.getImageData(imgX, imgY, 1, 1);

    const color = transform2rgba(data);

    onHover && onHover(imgX, imgY, color);

    if (isLock) return;

    this.nowPoint = point;
    // fix upper
    if (centerY < 0) {
      this.clearGlassRect();
    }
    this.glassCtx.clearRect(0, 0, glassWidth, glassHeight);
    if (scale < 1) {
      console.warn(
        `Can't make the galss scale less than 1, It will make bed invision`
      );
    }

    const finallyScale = INIT_NUMBER * (scale < 1 ? 1 : scale);
    drawImageSmoothingEnable(this.glassCtx, false);

    this.glassCtx.drawImage(
      this.imageCanvas,
      Math.floor(imgX - glassWidth / 2 / finallyScale),
      Math.floor(imgY - glassHeight / 2 / finallyScale),
      Math.floor(glassWidth / finallyScale),
      Math.floor(glassHeight / finallyScale),
      -INIT_NUMBER,
      -INIT_NUMBER,
      glassWidth,
      glassHeight
    );
    drawGrid(this.glassCtx, GRID_COLOR, INIT_NUMBER, INIT_NUMBER);
    drawCenterRect(
      this.glassCtx,
      CENTERGRID_COLOR,
      Math.floor(glassWidth / 2 - INIT_NUMBER),
      Math.floor(glassHeight / 2 - INIT_NUMBER),
      INIT_NUMBER,
      INIT_NUMBER
    );
    this.setState({
      x: imgX,
      y: imgY,
      glassLeft,
      glassTop,
      color
    });
  };

  handleClick = e => {
    const point = this.calculateCenterPoint(e);
    const { centerX, centerY, imgX, imgY } = point;
    const { data } = this.imageCtx.getImageData(imgX, imgY, 1, 1);
    const color = transform2rgba(data);
    const { onClick, isLock } = this.props;
    onClick && onClick(imgX, imgY, color);
    if (!isLock) this.nowPoint = point;
  };

  clearGlassRect = () => {
    const { glassHeight, glassWidth } = this.props;
    this.glassCtx.clearRect(0, 0, glassWidth, glassHeight);
    this.setState({ isShouldShowGlass: false });
  };

  handleMouseLeave = () => {
    this.clearGlassRect();
  };

  onKeyDown = e => {
    if (17 == e.keyCode) {
      this.setState({ isHoldCtrl: true });
    }
  };

  onKeyUp = e => {
    if (17 == e.keyCode) {
      this.setState({ isHoldCtrl: false });
    }
  };

  render() {
    const { rect, glassWidth, glassHeight, src, containerRef } = this.props;
    const {
      x,
      y,
      isShouldShowGlass,
      isHoldCtrl,
      glassLeft,
      glassTop,
      color
    } = this.state;
    const hexColor = rgb2hex_a(color).hex;
    const visibility = isShouldShowGlass && isHoldCtrl ? VISIBLE : HIDDEN;

    return (
      <div className={style.container} ref={containerRef}>
        <DocumentEvents onKeyDown={this.onKeyDown} />
        <DocumentEvents onKeyUp={this.onKeyUp} />
        <span className={style.infoX}>x: {x}</span>
        <span className={style.infoY}>y: {y}</span>
        <span className={style.infoColor}>
          color:{" "}
          <span
            className={style.infoBoxColor}
            style={{ backgroundColor: hexColor }}
          />{" "}
          {hexColor}
        </span>
        <img
          ref={this.imageContainerRef}
          className={style.image}
          crossOrigin="anonymous"
          src={src}
          onLoad={this.onLoadImg}
          onMouseMove={this.handleMove}
          onMouseLeave={this.handleMouseLeave}
          onClick={this.handleClick}
        />
        <canvas ref={this.imageCanvasRef} className={style.imageCanvas} />
        <canvas
          ref={this.rectCanvasRef}
          className={style.rectCanvas}
          onMouseMove={this.handleMove}
          onMouseLeave={this.handleMouseLeave}
          onClick={this.handleClick}
        />

        <div
          className={style.glass}
          style={{
            width: glassWidth,
            height: glassHeight,
            visibility,
            left: glassLeft,
            top: glassTop
          }}
          onClick={this.handleClick}
        >
          <canvas
            id="glassCanvas"
            ref={this.glassCanvasRef}
            width={glassWidth}
            height={glassHeight}
            style={{ width: glassWidth, height: glassHeight }}
          />
          <div
            className={style.glassText}
            style={{
              width: glassWidth,
              height: 20,
              top: glassHeight - 35
            }}
          >
            <div className={style.hexColor}>
              {hexColor}
              {"  "}
            </div>
          </div>
        </div>
      </div>
    );
  }
}
