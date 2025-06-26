import db_pool from "../db/connection.js";

/**
 * @typedef {object} User
 * @property {string} name
 * @property {string} email
 * @property {string} password_hash
 */

/**
 * @param {User} user
 * @returns {Promise<void>}
 */
async function createUser({ name, email, password_hash }) {
  const client = await db_pool.connect();
  try {
    await client.query(
      `INSERT INTO "User" (name, email, password_hash) VALUES ($1, $2, $3)`,
      [name, email, password_hash]
    );
  } catch (err) {
    throw new Error(`Create User Failed: ${err.message}`);
  } finally {
    client.release();
  }
}

/**
 * @param {string} email
 * @returns {Promise<User|null>}
 */
async function readUser(email) {
  const client = await db_pool.connect();
  try {
    const res = await client.query(
      `SELECT name, email, password_hash FROM "User" WHERE email=$1 LIMIT 1`,
      [email]
    );
    if (res.rows.length == 0) return null;
    return res.rows[0];
  } catch (err) {
    throw new Error(`Database Select Failed: ${err.message}`);
  } finally {
    client.release();
  }
}

export { createUser, readUser };
