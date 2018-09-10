const path = require("path");
const webpack = require("webpack");
// const ExtractTextWebpackPlugin = require("extract-text-webpack-plugin");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const env = process.env.NODE_ENV || "development";

// const extractCSS = new ExtractTextPlugin('stylesheets/[name]-one.css');
resolve = p => path.resolve(__dirname, p);

module.exports = {
  target: "electron-renderer",

  devtool: "source-map",
  entry: {
    app: "./src/renderer_process.js"
  },
  output: {
    path: path.join(__dirname, "build", "dev"),
    publicPath: "",
    filename: "bundle-[name].js"
  },

  module: {
    rules: [
      {
        test: /\.jsx?$/,
        loader: "babel-loader",
        exclude: /node_modules/,
        options: {
          presets: ["es2015", "react", "stage-0", "stage-2"],
          plugins: ["transform-runtime", "transform-decorators-legacy"]
        }
      },
      {
        test: /\.s?css$/,
        use: [
          { loader: "style-loader" },
          {
            loader: "css-loader",
            options: {
              modules: false,
              localIdentName: "[path][name]__[local]--[hash:base64:5]"
            }
          }
        ]
      },
      {
        test: /\.(png|jpg|gif|svg)$/,
        loader: "file-loader",
        options: {
          name: "[name].[ext]?[hash]"
        }
      },
      {
        test: /\.(woff|woff2)(\?v=\d+\.\d+\.\d+)?$/,
        loader: "url-loader",
        options: {
          limit: 10000,
          mimetype: "application/font-woff"
        }
      },
      {
        test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/,
        loader: "url-loader",
        options: {
          limit: 10000,
          mimetype: "application/octet-stream"
        }
      },
      {
        test: /\.eot(\?v=\d+\.\d+\.\d+)?$/,
        loader: "file-loader",
        options: {
          name: "[name].[ext]?[hash]"
        }
      },
      {
        test: /\.svg(\?v=\d+\.\d+\.\d+)?$/,
        loader: "url-loader",
        options: {
          limit: 10000,
          mimetype: "image/svg+xml"
        }
      }
    ]
  },

  plugins: [
    new HtmlWebpackPlugin({
      title: "UI嗅探工具",
      template: path.join(__dirname, "src", "index.html")
    }),
    new webpack.ProgressPlugin(),
    new webpack.HotModuleReplacementPlugin(),
    new webpack.ProvidePlugin({
      Popper: ["popper.js", "default"]
    }),
    new webpack.DefinePlugin({
      "process.env": { NODE_ENV: JSON.stringify("development") }
    }),
    new webpack.IgnorePlugin(/^\.\/src\/(adb|logcat|monkey)$/)
  ],

  resolve: {
    extensions: [".js", ".json", ".jsx"],

    // 默认路径代理
    // 例如 import Vue from 'vue'，会自动到 'vue/dist/vue.common.js'中寻找
    alias: {
      "@": resolve("src"),
      "@assets": resolve("src/assets"),
      "@pages": resolve("src/pages"),
      "@history": resolve("src/history"),
      "@components": resolve("src/components"),
      "@reducers": resolve("src/reducers")
    }
  }
};
