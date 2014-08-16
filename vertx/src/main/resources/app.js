"use strict";

var vertx = require("vertx");
var container = require("vertx/container");
var eventBus = require('vertx/event_bus');
var http = require("vertx/http");
var matcher = new http.RouteMatcher();

matcher.noMatch(function(req) {
    if (req.method() === "HEAD") {
        req.response.end();
    } else {
        var fileName = req.path() === "/" ? "web/index.html" : "web" + req.path();
        req.response.sendFile(fileName);
    }
});

var sockJSAddress = "sockjs-server-address";

eventBus.registerHandler(sockJSAddress, function(message, replier) {
    container.logger.info("Got message: " + message + "\tReply it now.");
    if (replier) {
        replier("Got your message: " + message);
    }
});
container.logger.info("Register handler on address: " + sockJSAddress);

var server = http.createHttpServer()
        .requestHandler(matcher);

var sockJSServer = vertx.createSockJSServer(server);
sockJSServer.bridge({prefix : '/eventbus'}, [{}], [{}]);

var port = container.config["port"] || 8090;
server.listen(port);
container.logger.info("Server listening on port " + port);