import { Socket } from "socket.io";
import jwt from "jsonwebtoken";

/**
 * Socket middleware for authentication
 * @param {Socket} socket
 * @param {(Error|void) => void} next
 */
const socketVerifyUser = (socket, next) => {
  const authHeader = socket.handshake.headers.authorization;
  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    return next(new Error("Authentication Failure"));
  }

  const authToken = authHeader.split(" ")[1];
  jwt.verify(authToken, process.env.JWT_SECRET, (err, decoded) => {
    if (err) {
      return next(new Error("Authentication Failure"));
    }
    socket.user = decoded;
    next();
  });
};

export default socketVerifyUser;
