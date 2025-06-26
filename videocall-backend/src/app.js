import express from "express";
import authRoute from "./routes/auth.route.js";
import roomRoute from "./routes/room.route.js";
import { createServer } from "http";
import { Server } from "socket.io";
import socketHandler from "./signaling/socket-handler.js";
import socketVerifyUser from "./middlewares/socket-verify-user.middleware.js";
import socketValidateRoom from "./middlewares/socket-room-validation.middleware.js";

const app = express();
const server = createServer(app);
const io = new Server(server, {
  cors: {
    origin: "*",
    methods: ["GET", "POST"]
  }
});

app.use(express.json());
app.use("/auth", authRoute);
app.use("/room", roomRoute);

io.use(socketVerifyUser);
io.use(socketValidateRoom);
socketHandler(io);

app.get("/", (_, res) => {
  res.send("Hello World");
});

export { server };
