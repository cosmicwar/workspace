package scripts
//package scripts.servers.nexus
//
//import com.google.gson.Gson
//import com.google.gson.JsonArray
//import com.google.gson.JsonElement
//import com.google.gson.JsonObject
//import com.neovisionaries.ws.client.ThreadType
//import com.neovisionaries.ws.client.WebSocket
//import com.neovisionaries.ws.client.WebSocketException
//import com.neovisionaries.ws.client.WebSocketFactory
//import com.neovisionaries.ws.client.WebSocketFrame
//import com.neovisionaries.ws.client.WebSocketListener
//import com.neovisionaries.ws.client.WebSocketState
//import org.starcade.starlight.enviorment.Exports
//import org.starcade.starlight.enviorment.GroovyScript
//import org.starcade.starlight.helper.Schedulers
//import scripts.shared.utils.Exports2
//import scripts.shared3.Redis
//
//@Grab("com.neovisionaries:nv-websocket-client:2.14")
//class Playwire {
//    static Gson gson = new Gson();
//    static String serverUrl = Exports.ptr("Playwire:serverUrl")
//    static String serverToken = Exports.ptr("Playwire:serverToken")
//
//    WebSocket websocket;
//
//    Playwire() {
//        Schedulers.async().runRepeating({
//            if (websocket == null || !websocket.isOpen()) {
//                openWebSocket();
//            }
//        }, 0L, 1000L);
//
//        GroovyScript.addUnloadHook {
//            websocket.sendClose();
//        }
//    }
//
//    void openWebSocket() {
//        String endpoint = "ws://${serverUrl}/cable?token=" + serverToken;
//        println("using endpoint: ${endpoint}")
//
//        websocket = new WebSocketFactory().setVerifyHostname(false).createSocket(endpoint);
//        websocket.addListener(new WebSocketListener() {
//            @Override
//            void onConnected(WebSocket webSocket, Map<String, List<String>> map) throws Exception {
//                println("[playwire] connected");
//            }
//
//            @Override
//            void onConnectError(WebSocket webSocket, WebSocketException e) throws Exception {
//                println("[playwire] error: ${e.error}");
//            }
//
//            @Override
//            void onDisconnected(WebSocket webSocket, WebSocketFrame webSocketFrame, WebSocketFrame webSocketFrame1, boolean b) throws Exception {
//                println("[playwire] disconnected");
//            }
//
//            @Override
//            void onTextMessage(WebSocket webSocket, String s) throws Exception {
//                JsonObject message = gson.fromJson(s, JsonObject);
//                JsonElement type = message.get("type");
//
//                if (type != null && type.isJsonPrimitive()) {
//                    switch (type.asString) {
//                        case "welcome": {
//                            println("[playwire] welcome");
//
//                            JsonObject channelSubscribe = new JsonObject();
//                            channelSubscribe.addProperty("command", "subscribe");
//
//                            JsonObject identifier = new JsonObject();
//                            identifier.addProperty("channel", "ServerNotificationChannel")
//
//                            channelSubscribe.addProperty("identifier", gson.toJson(identifier));
//
//                            webSocket.sendText(gson.toJson(channelSubscribe));
//                            break
//                        }
//                        case "confirm_subscription": {
//                            println("[playwire] subscribed to channel " + message.get("identifier").asString);
//                            break
//                        }
//                        case "ping": {
//                            return
//                        }
//                    }
//                } else {
//                    JsonElement channelMessage = message.get("message");
//
//                    if (channelMessage != null && channelMessage.isJsonObject()) {
//                        //println("raw: ${message}");
//                        JsonObject data = channelMessage.asJsonObject.get("data").asJsonObject;
//                        JsonObject attributes = data.get("attributes").asJsonObject;
//
//                        String aasmState = attributes.get("aasm-state").asString;
//
//                        JsonArray included = channelMessage.asJsonObject.get("included").asJsonArray;
//
//                        JsonObject players = (included.find { it.asJsonObject.get("type").asString == "players" } as JsonElement).asJsonObject
//
//                        String uuid = players.get("attributes").asJsonObject.get("uuid").asString
//
//                        println("registered ad-view for ${uuid}: ${aasmState}")
//
//                        Exports2.invoke("Interactions", "player_ad_event", uuid, aasmState);
//                    }
//                }
//
//            }
//
//            @Override
//            void onTextMessage(WebSocket webSocket, byte[] bytes) throws Exception {}
//
//            @Override
//            void onStateChanged(WebSocket webSocket, WebSocketState webSocketState) throws Exception {}
//
//            @Override
//            void onFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {}
//
//            @Override
//            void onContinuationFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {}
//
//            @Override
//            void onTextFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {}
//
//            @Override
//            void onBinaryFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {}
//
//            @Override
//            void onCloseFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {}
//
//            @Override
//            void onPingFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {}
//
//            @Override
//            void onPongFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {}
//
//            @Override
//            void onBinaryMessage(WebSocket webSocket, byte[] bytes) throws Exception {}
//
//            @Override
//            void onSendingFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {}
//
//            @Override
//            void onFrameSent(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {}
//
//            @Override
//            void onFrameUnsent(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {}
//
//            @Override
//            void onThreadCreated(WebSocket webSocket, ThreadType threadType, Thread thread) throws Exception {}
//
//            @Override
//            void onThreadStarted(WebSocket webSocket, ThreadType threadType, Thread thread) throws Exception {}
//
//            @Override
//            void onThreadStopping(WebSocket webSocket, ThreadType threadType, Thread thread) throws Exception {}
//
//            @Override
//            void onError(WebSocket webSocket, WebSocketException e) throws Exception {}
//
//            @Override
//            void onFrameError(WebSocket webSocket, WebSocketException e, WebSocketFrame webSocketFrame) throws Exception {}
//
//            @Override
//            void onMessageError(WebSocket webSocket, WebSocketException e, List<WebSocketFrame> list) throws Exception {}
//
//            @Override
//            void onMessageDecompressionError(WebSocket webSocket, WebSocketException e, byte[] bytes) throws Exception {}
//
//            @Override
//            void onTextMessageError(WebSocket webSocket, WebSocketException e, byte[] bytes) throws Exception {}
//
//            @Override
//            void onSendError(WebSocket webSocket, WebSocketException e, WebSocketFrame webSocketFrame) throws Exception {}
//
//            @Override
//            void onUnexpectedError(WebSocket webSocket, WebSocketException e) throws Exception {
//                e.printStackTrace()
//            }
//
//            @Override
//            void handleCallbackError(WebSocket webSocket, Throwable throwable) throws Exception {
//                throwable.printStackTrace();
//            }
//
//            @Override
//            void onSendingHandshake(WebSocket webSocket, String s, List<String[]> list) throws Exception {}
//        });
//        websocket.connect();
//
//    }
//}