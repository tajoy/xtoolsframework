// Basic init
const path = require("path");
const electron = require("electron");
const { app, BrowserWindow } = electron;

// Let electron reloads by itself when webpack watches changes in ./src/
require("electron-reload")(path.join(__dirname, "src"));

// To avoid being garbage collected
let mainWindow;

// 当所有窗口被关闭了，退出。
app.on("window-all-closed", function() {
  app.quit();
});

app.on("ready", () => {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 960,
    minWidth: 1280,
    minHeight: 960,
    title: "UI嗅探工具"
  });

  mainWindow.loadURL(`file://${__dirname}/build/dev/index.html`);
  mainWindow.on("close", () => app.quit());
});
