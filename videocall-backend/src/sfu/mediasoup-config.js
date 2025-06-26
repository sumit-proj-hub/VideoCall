import mediasoup from "mediasoup";

const workerSettings = {
  logLevel: "debug", // “debug”, “warn”, “error” or “none”
  logTags: ["info", "ice", "dtls", "rtp", "srtp", "rtcp", "rbe", "rtx"] // as needed
};

const routerOptions = {
  mediaCodecs: [
    {
      kind: "audio",
      mimeType: "audio/opus",
      clockRate: 48000,
      channels: 2
    },
    {
      kind: "video",
      mimeType: "video/VP8",
      clockRate: 90000,
      parameters: {
        "x-google-start-bitrate": 1000
      }
    }
  ]
};

const transportOptions = {
  listenInfos: [{ ip: "0.0.0.0", announcedAddress: process.env.PUBLIC_IP }],
  enableUdp: true,
  enableTcp: true,
  preferUdp: true
};

/** @type {mediasoup.types.Worker} */
let worker;

/** @returns {Promise<void>} */
const createWorker = async () => {
  worker = await mediasoup.createWorker(workerSettings);
  worker.on("died", () => {
    console.error("MediaSoup worker has died");
  });
};

/** @returns {Promise<mediasoup.types.Router>} */
const createRouter = () => worker.createRouter(routerOptions);

/**
 * @param {mediasoup.types.Router} router
 * @returns {Promise<mediasoup.types.Transport|null>}
 */
const createWebRtcTransport = async (router) => {
  try {
    const transport = await router.createWebRtcTransport(transportOptions);

    transport.on("dtlsstatechange", (dtlsState) => {
      if (dtlsState === "closed") {
        transport.close();
      }
    });

    transport.on("close", () => {
      console.log("transport closed");
    });

    return transport;
  } catch (error) {
    console.error(error);
    return null;
  }
};

export { createWorker, createRouter, createWebRtcTransport, worker };
