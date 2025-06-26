import db_pool from "../db/connection.js";
/**
 * @param {string} hostEmail
 * @param {string} roomName
 * @returns {Promise<number>}
 */
async function createRoom(hostEmail, roomName) {
  const query = `--sql
    INSERT INTO "Room" (room_owner_id, room_name)
    VALUES ((SELECT id FROM "User" WHERE email = $1), $2)
    RETURNING id;
  `;

  const client = await db_pool.connect();
  try {
    const res = await client.query(query, [hostEmail, roomName]);
    return res.rows[0].id;
  } catch (err) {
    throw new Error(`Create Room Failed: ${err.message}`);
  } finally {
    client.release();
  }
}

/**
 * @typedef {object} Room
 * @property {number} id
 * @property {string} room_name
 * @property {string} room_owner_name
 * @property {string} room_owner_email
 * @property {number} created_on_epoch
 */

/**
 * @param {string} roomId
 * @returns {Promise<Room|null>}
 */
async function readRoom(roomId) {
  const query = `--sql
    SELECT
      r.id AS id,
      r.room_name AS room_name,
      u.name AS room_owner_name,
      u.email AS room_owner_email,
      EXTRACT(EPOCH FROM r.created_on) AS created_on_epoch
    FROM "Room" r
    JOIN "User" u
    ON r.room_owner_id = u.id
    WHERE r.id = $1
    LIMIT 1;
  `;

  const client = await db_pool.connect();
  try {
    const res = await client.query(query, [roomId]);
    if (res.rows.length == 0) return null;
    return res.rows[0];
  } catch (err) {
    throw new Error(`Read Room Failed: ${err.message}`);
  } finally {
    client.release();
  }
}

/**
 * @param {string} hostEmail
 * @returns {Promise<Room[]>}
 */
async function readRoomsByOwner(hostEmail) {
  const query = `--sql
    SELECT
      r.id AS id,
      r.room_name AS room_name,
      u.name AS room_owner_name,
      u.email AS room_owner_email,
      EXTRACT(EPOCH FROM r.created_on) AS created_on_epoch
    FROM "Room" r
    JOIN "User" u
    ON r.room_owner_id = u.id
    WHERE u.email = $1;
  `;

  const client = await db_pool.connect();
  try {
    const res = await client.query(query, [hostEmail]);
    return res.rows;
  } catch (err) {
    throw new Error(`Read Rooms By Owner Failed: ${err.message}`);
  } finally {
    client.release();
  }
}

/**
 * @param {number} roomId
 * @returns {Promise<void>}
 */
async function deleteRoom(roomId) {
  const query = `--sql
    DELETE FROM "Room"
    WHERE id = $1;
  `;

  const client = await db_pool.connect();
  try {
    await client.query(query, [roomId]);
  } catch (err) {
    throw new Error(`Delete Room Failed: ${err.message}`);
  } finally {
    client.release();
  }
}

export { createRoom, readRoom, readRoomsByOwner, deleteRoom };
