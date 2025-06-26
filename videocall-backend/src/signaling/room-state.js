/**
 * @typedef {object} PeerInfo
 * @property {{name: string, email: string}} userData
 * @property {import("mediasoup/types").Transport|null} sendTransport
 * @property {import("mediasoup/types").Transport|null} recvTransport
 * @property {import("mediasoup/types").Producer|null} videoProducer
 * @property {import("mediasoup/types").Producer|null} audioProducer
 * @property {Map<number, import("mediasoup/types").Consumer>} consumers
 * @property {boolean} isMicOn
 * @property {boolean} isVideoOn
 */

/**
 * @typedef {number} RoomId
 * @typedef {string} SocketId
 */

/**
 * @typedef {object} RoomState
 * @property {import("mediasoup/types").Router} router
 * @property {Map<SocketId, PeerInfo>} peerMap
 */

/** @type {Map<RoomId, RoomState>} */
const roomState = new Map();

export default roomState;
