"use strict";

var container = require("vertx/container");
var app = require("app");

container.deployVerticle(app, container.config, function(err, deployID) {
  if (!err) {
    container.logger.debug("The app verticle has been deployed, deployment ID is " + deployID);
  } else {
    container.logger.debug("App verticle deployment failed! " + err.getMessage());
  }
});