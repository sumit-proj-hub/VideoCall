import { Router } from "express";
import register from "../controllers/register-user.controller.js";
import login from "../controllers/login-user.controller.js";

const router = Router();

router.post("/register", register);
router.post("/login", login);

export default router;
