import request from "request";
import { NET_SEND_MSG } from "@reducers";

const TESTING = false;//process.env.NODE_ENV === "development";
import testData from "./testData.js";

class SendContext {
  constructor(url, msg) {
    const headers = {
      "Accept-Charset": "utf-8",
      Accept: "application/json",
      "Content-Type": "application/json;charset=utf-8",
      Connection: "Keep-Alive"
    };
    const self = this;
    if (TESTING && url.endsWith("/dump_ui")) {
      var timerId;
      timerId = setInterval(() => {
        self.func_success(testData);
        clearInterval(timerId);
      }, 500);
    } else {
      this.req = request(url, {
        method: "POST",
        body: msg,
        json: true,
        encoding: "utf8",
        headers: headers,
        callback: (err, httpResponse, body) =>
          this.onCallback(err, httpResponse, body)
      });
    }
    this.func_success = null;
    this.func_fail = null;
  }

  success(func) {
    this.func_success = func;
    return this;
  }

  fail(func) {
    this.func_fail = func;
    return this;
  }

  onCallback(err, httpResponse, body) {
    if (err) {
      console.error("fail:", err);
      if (this.func_fail) this.func_fail(err);
    } else {
      console.info("success:", err);
      if (this.func_success) this.func_success(body);
    }
  }
}

export function sendMsg(url, msg) {
  const context = new SendContext(url, msg);
  return context;
}

export function sendMockMsg(url, msg) {
  const context = new SendContext(url + "/mock_event", msg);
  return context;
}