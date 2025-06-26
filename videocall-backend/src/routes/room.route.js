import { Router } from "express";
import {
  createRoomHandler,
  getRoom,
  getRoomsByOwner,
  deleteRoomHandler
} from "../controllers/room.controller.js";
import verifyUser from "../middlewares/verify-user.middleware.js";

const router = Router();

router.post("/createRoom", verifyUser, createRoomHandler);
router.get("/getRoom", getRoom);
router.get("/getUserRooms", verifyUser, getRoomsByOwner);
router.post("/deleteRoom", verifyUser, deleteRoomHandler);

export default router;
