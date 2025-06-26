import bcrypt from "bcrypt";
import jwt from "jsonwebtoken";

/**
 * Validates input email
 * @param {string} email
 * @returns {boolean}
 */
function isValidEmail(email) {
  const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  return regex.test(email);
}

/**
 * Generates password hash for given password
 * @param {string} password
 * @returns {Promise<string>}
 */
async function hashPassword(password) {
  const saltRounds = 10;
  return await bcrypt.hash(password, saltRounds);
}

/**
 * Verifies input password from stored hash
 * @param {string} inputPassword
 * @param {string} storedHash
 * @returns {Promise<boolean>}
 */
async function verifyPassword(inputPassword, storedHash) {
  return await bcrypt.compare(inputPassword, storedHash);
}

/**
 * Generates JWT token
 * @param {string} name
 * @param {string} email
 * @returns {string}
 */
function generateToken(name, email) {
  return jwt.sign(
    {
      name: name,
      email: email
    },
    process.env.JWT_SECRET
  );
}

export { isValidEmail, hashPassword, verifyPassword, generateToken };
