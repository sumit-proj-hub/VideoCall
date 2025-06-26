/**
 * @typedef {Object} PeerSummary
 * @property {string} socketId
 * @property {{ name: string, email: string }} userData
 * @property {boolean} isMicOn
 * @property {boolean} isVideoOn
 */

/**
 * Get peer summaries of all peers
 * @param {import("./room-state").RoomState} roomState
 * @returns {PeerSummary[]}
 */
export default function getAllPeers(roomState) {
  const peerSummaries = [];

  for (const [socketId, peerInfo] of roomState.peerMap.entries()) {
    peerSummaries.push({
      socketId,
      name: peerInfo.userData.name,
      email: peerInfo.userData.email,
      isMicOn: peerInfo.isMicOn,
      isVideoOn: peerInfo.isVideoOn
    });
  }

  return peerSummaries;
}
