import { Server } from "socket.io";
import roomState from "./room-state.js";
import { createRouter } from "../sfu/mediasoup-config.js";
import initSession from "./init-session.js";
import transportConnect from "./transport-connect.js";
import transportProduce from "./transport-produce.js";
import consumeProducer from "./consume-producer.js";
import changeConsumerState from "./change-consumer-state.js";
import getAllPeers from "./get-all-peers.js";

/**
 * Socket Signaling Server Handler
 * @param {Server} io
 */
const socketHandler = (io) => {
  io.on("connection", async (socket) => {
    console.log(`User ${socket.id} connected`);

    /** @type {number} */
    const roomId = socket.roomInfo.id;
    const roomIdStr = roomId.toString();
    if (!roomState.has(roomId)) {
      roomState.set(roomId, {
        router: await createRouter(),
        peerMap: new Map()
      });
    }

    /** @type {import("./room-state.js").RoomState} */
    const curRoom = roomState.get(roomId);

    /** @type {import("./room-state.js").PeerInfo} */
    const curPeer = {
      userData: socket.user,
      sendTransport: null,
      recvTransport: null,
      videoProducer: null,
      audioProducer: null,
      consumers: new Map(),
      isMicOn: false,
      isVideoOn: false
    };
    curRoom.peerMap.set(socket.id, curPeer);

    socket.on("initSession", async (callback) => {
      await initSession(socket, curPeer, curRoom, roomIdStr, callback);
    });

    socket.on("transportConnect", async ({ isSender, dtlsParameters }, callback) => {
      await transportConnect(curPeer, isSender, dtlsParameters, callback);
    });

    socket.on("transportProduce", async ({ kind, rtpParameters }, callback) => {
      await transportProduce(
        socket,
        roomIdStr,
        curPeer,
        kind,
        rtpParameters,
        callback
      );
    });

    socket.on("getAllPeers", (callback) => callback(getAllPeers(curRoom)));

    socket.on("clientMicToggle", (isMicOn) => {
      curPeer.isMicOn = isMicOn;
      socket.to(roomIdStr).emit("userMicToggle", {
        socketId: socket.id,
        isMicOn,
        isVideoOn: curPeer.isVideoOn
      });
    });

    socket.on("clientVideoToggle", (isVideoOn) => {
      curPeer.isVideoOn = isVideoOn;
      socket.to(roomIdStr).emit("userVideoToggle", {
        socketId: socket.id,
        isVideoOn,
        isMicOn: curPeer.isMicOn
      });
    });

    socket.on(
      "consumeProducer",
      async ({ producerSocketId, kind, rtpCapabilities }, callback) => {
        await consumeProducer(
          curPeer,
          curRoom,
          producerSocketId,
          kind,
          rtpCapabilities,
          callback
        );
      }
    );

    socket.on("changeConsumerState", async ({ consumerId, state }) => {
      await changeConsumerState(curPeer, consumerId, state);
    });

    socket.on("disconnect", (reason) => {
      console.log(`User ${socket.id} disconnected. Reason: ${reason}`);

      socket.to(roomIdStr).emit("userLeft", {
        socketId: socket.id,
        userData: curPeer.userData
      });
      socket.leave(roomIdStr);

      if (curPeer.sendTransport) curPeer.sendTransport.close();
      if (curPeer.recvTransport) curPeer.recvTransport.close();

      curRoom.peerMap.delete(socket.id);
      if (curRoom.peerMap.size === 0) {
        curRoom.router.close();
        roomState.delete(roomId);
      }
    });
  });
};

export default socketHandler;
