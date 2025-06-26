import { verifyPassword, generateToken } from "../utils/auth-utils.js";
import { readUser } from "../models/user.model.js";

const login = async (req, res) => {
  const { email, password } = req.body;
  const errors = [];

  if (typeof email !== "string") {
    errors.push("invalid email");
  }

  if (typeof password !== "string") {
    errors.push("invalid password");
  }

  if (errors.length > 0) {
    return res.status(400).json({ errors: errors, success: false });
  }

  try {
    const user = await readUser(email);
    if (
      user === null ||
      !(await verifyPassword(password, user.password_hash))
    ) {
      return res
        .status(401)
        .json({ success: false, message: "Invalid Credentials" });
    }
    const jwtToken = generateToken(user.name, user.email);
    res.status(200).json({ token: jwtToken, success: true });
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false });
  }
};

export default login;
