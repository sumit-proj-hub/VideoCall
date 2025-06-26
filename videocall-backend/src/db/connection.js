import pg from "pg";

const db_pool = new pg.Pool({
  user: process.env.PG_USER,
  host: process.env.PG_HOST,
  database: process.env.PG_DATABASE,
  password: process.env.PG_PASS,
  port: process.env.PG_PORT,
  max: 10,
  idleTimeoutMillis: 30000
});

export default db_pool;
