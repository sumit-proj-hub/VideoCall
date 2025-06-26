import "dotenv/config";
import { server } from "./app.js";
import db_pool from "./db/connection.js";
import { createWorker } from "./sfu/mediasoup-config.js";

db_pool
  .connect()
  .then(() => createWorker().then())
  .then(() => {
    const PORT = process.env.PORT;
    server.listen(PORT, "0.0.0.0", () => {
      console.log(`Server is running on port ${PORT}`);
    });
  })
  .catch((err) => {
    console.log(`Failed to connect database: ${err}`);
  });
