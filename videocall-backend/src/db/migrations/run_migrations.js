import "dotenv/config";
import fs from "fs";
import db_pool from "../connection.js";

const create_user_table_query = fs
  .readFileSync("src/db/migrations/1_create_user_table.sql")
  .toString();

const create_room_table_query = fs
  .readFileSync("src/db/migrations/2_create_room_table.sql")
  .toString();

async function runMigrations() {
  const client = await db_pool.connect();
  await client.query(create_user_table_query);
  await client.query(create_room_table_query);
  client.release();
  await db_pool.end();
}

runMigrations();
