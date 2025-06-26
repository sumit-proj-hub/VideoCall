import { Socket } from "socket.io";
import { createWebRtcTransport } from "../sfu/mediasoup-config.js";

/**
 * @callback InitSessionCallback
 * @param {object} result
 * @param {string} [result.error]
 * @param {object} [result.sendTransportParams]
 * @param {object} [result.recvTransportParams]
 * @param {import("mediasoup/types").RtpCapabilities} [result.rtpCapabilities]
 * @returns {void}
 */

/**
 * Initializes the transports of new peer and informs other peers about the new peer
 * @param {Socket} socket
 * @param {import("./room-state").PeerInfo} curPeer
 * @param {import("./room-state").RoomState} curRoom
 * @param {string} roomIdStr
 * @param {InitSessionCallback} callback
 * @returns {Promise<void>}
 */
export default async function initSession(
  socket,
  curPeer,
  curRoom,
  roomIdStr,
  callback
) {
  if (curPeer.sendTransport !== null || curPeer.recvTransport !== null) {
    callback({ error: "Session already initialized" });
    return;
  }

  curPeer.sendTransport = await createWebRtcTransport(curRoom.router);
  curPeer.recvTransport = await createWebRtcTransport(curRoom.router);

  if (curPeer.sendTransport === null || curPeer.recvTransport === null) {
    callback({ error: "Failed to create transports" });
    return;
  }

  callback({
    sendTransportParams: {
      id: curPeer.sendTransport.id,
      iceParameters: curPeer.sendTransport.iceParameters,
      iceCandidates: curPeer.sendTransport.iceCandidates,
      dtlsParameters: curPeer.sendTransport.dtlsParameters
    },
    recvTransportParams: {
      id: curPeer.recvTransport.id,
      iceParameters: curPeer.recvTransport.iceParameters,
      iceCandidates: curPeer.recvTransport.iceCandidates,
      dtlsParameters: curPeer.recvTransport.dtlsParameters
    },
    rtpCapabilities: curRoom.router.rtpCapabilities
  });

  socket.join(roomIdStr);
  socket.to(roomIdStr).emit("userJoined", {
    socketId: socket.id,
    name: curPeer.userData.name,
    email: curPeer.userData.email,
    isMicOn: curPeer.isMicOn,
    isVideoOn: curPeer.isVideoOn
  });
}
