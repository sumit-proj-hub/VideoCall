/**
 * @callback ConsumeProducerCallback
 * @param {object} result
 * @param {string} [result.error]
 * @param {string} [result.id]
 * @param {string} [result.producerId]
 * @param {string} [result.kind]
 * @param {object} [result.rtpParameters]
 * @returns {void}
 */

/**
 * Creates a consumer for given producer
 * @param {import("./room-state").PeerInfo} curPeer
 * @param {import("./room-state").RoomState} curRoom
 * @param {import("./room-state").SocketId} producerSocketId
 * @param {string} kind
 * @param {import("mediasoup/types").RtpCapabilities} rtpCapabilities
 * @param {ConsumeProducerCallback} callback
 */
export default async function consumeProducer(
  curPeer,
  curRoom,
  producerSocketId,
  kind,
  rtpCapabilities,
  callback
) {
  if (
    typeof producerSocketId !== "string" ||
    (kind !== "video" && kind !== "audio") ||
    typeof rtpCapabilities !== "object"
  ) {
    callback({ error: "Invalid Parameters" });
    return;
  }

  if (!curRoom.peerMap.has(producerSocketId)) {
    callback({ error: "Producer Not Found" });
    return;
  }

  const peer = curRoom.peerMap.get(producerSocketId);
  const producer = kind === "video" ? peer.videoProducer : peer.audioProducer;

  if (producer === null) {
    callback({ error: "Producer Not Created" });
    return;
  }

  try {
    if (
      curRoom.router.canConsume({ producerId: producer.id, rtpCapabilities })
    ) {
      const consumer = await curPeer.recvTransport.consume({
        producerId: producer.id,
        rtpCapabilities,
        paused: true
      });

      curPeer.consumers.set(consumer.id, consumer);

      callback({
        id: consumer.id,
        producerId: producer.id,
        kind: consumer.kind,
        rtpParameters: consumer.rtpParameters
      });
    }
  } catch (error) {
    callback({ error: error });
  }
}
