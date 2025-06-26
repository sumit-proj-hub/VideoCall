import { Socket } from "socket.io";
import { readRoom } from "../models/room.model.js";

/**
 * Socket middleware for authentication
 * @param {Socket} socket
 * @param {(Error|void) => void} next
 */
const socketValidateRoom = async (socket, next) => {
  const { roomId } = socket.handshake.query;
  if (!roomId) {
    return next(Error("Room Id Required"));
  }

  const roomInfo = await readRoom(roomId);
  if (roomInfo === null) {
    return next(Error("Invalid Room Id"));
  }

  socket.roomInfo = roomInfo;
  next();
};

export default socketValidateRoom;
