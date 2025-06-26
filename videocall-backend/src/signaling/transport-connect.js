/**
 * Sets dtlsParameters and connects to transport
 * @param {import("./room-state").PeerInfo} curPeer
 * @param {boolean} isSender
 * @param {object} dtlsParameters
 * @param {() => void} callback
 * @returns {Promise<void>}
 */
export default async function transportConnect(
  curPeer,
  isSender,
  dtlsParameters,
  callback
) {
  if (typeof isSender !== "boolean" || typeof dtlsParameters !== "object")
    return;
  if (isSender) await curPeer.sendTransport.connect({ dtlsParameters });
  else await curPeer.recvTransport.connect({ dtlsParameters });
  callback();
}
