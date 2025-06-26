/**
 * Changes consumer state to resumed or paused
 * @param {import("./room-state").PeerInfo} curPeer
 * @param {string} consumerId
 * @param {boolean} state - true to resume and false to pause consumer
 * @returns
 */
export default async function changeConsumerState(curPeer, consumerId, state) {
  if (typeof consumerId !== "string" || typeof state !== "boolean") return;
  const consumer = curPeer.consumers.get(consumerId);
  if (consumer !== undefined) {
    if (state) await consumer.resume();
    else await consumer.pause();
  }
}
