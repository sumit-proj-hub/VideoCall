import { Socket } from "socket.io";

/**
 * Creates a producer
 * @param {Socket} socket
 * @param {string} roomIdStr
 * @param {import("./room-state").PeerInfo} curPeer
 * @param {string} kind
 * @param {import("mediasoup/types").RtpParameters} rtpParameters
 * @param {(result: {id: string} | {error: string}) => void} callback
 * @returns {Promise<void>}
 */
export default async function transportProduce(
  socket,
  roomIdStr,
  curPeer,
  kind,
  rtpParameters,
  callback
) {
  if (
    (kind !== "audio" && kind !== "video") ||
    typeof rtpParameters !== "object"
  ) {
    callback({ error: "Invalid Parameters" });
    return;
  }

  const producer = await curPeer.sendTransport.produce({
    kind,
    rtpParameters
  });
  producer.on("transportclose", () => producer.close());

  if (kind === "audio") {
    if (curPeer.audioProducer !== null) curPeer.audioProducer.close();
    curPeer.audioProducer = producer;
  } else {
    if (curPeer.videoProducer !== null) curPeer.videoProducer.close();
    curPeer.videoProducer = producer;
  }

  socket.to(roomIdStr).emit("producerCreated", {
    socketId: socket.id,
    kind: kind,
    producerId: producer.id
  });

  callback({ id: producer.id });
}
