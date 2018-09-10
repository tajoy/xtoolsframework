// Basic init
const path = require("path");
const electron = require("electron");
const { app, BrowserWindow } = electron;

// To avoid being garbage collected
let mainWindow;

// 当所有窗口被关闭了，退出。
app.on("window-all-closed", function() {
  app.quit();
});

let url = `file://${__dirname}/build/dist/index.html`;

app.setPath("userData", app.getPath("userData").replace("Electron", "bank-tools-inspector"))

try {
  console.log("userData: " + app.getPath("userData"));
  console.log("module: " + app.getPath("module"));
  console.log("logs: " + app.getPath("logs"));
  console.log(
    "pepperFlashSystemPlugin: " + app.getPath("pepperFlashSystemPlugin")
  );
} catch (error) {
  console.error(error);
}

app.on("ready", () => {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 960,
    minWidth: 1280,
    minHeight: 960,
    center: true,
    title: "UI嗅探工具",
    backgroundColor: "#2e2c29",
    show: false,
    webPreferences: {
      plugins: true
    }
  });
  mainWindow.on("close", () => app.quit());

  let contents = mainWindow.webContents;
  mainWindow.once("ready-to-show", () => {
    mainWindow.show();
  });
  contents.on("console-message", (event, line, message, sourceId) => {
    console.log(message);
  });
  console.log("load url: " + url);
  mainWindow.loadURL(url);
});
