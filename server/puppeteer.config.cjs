const { join } = require('path');

/**
 * @type {import("puppeteer").Configuration}
 */
module.exports = {
  // Directs Puppeteer and @puppeteer/browsers to store Chrome inside the project directory
  // so Render preserves and uploads it to production.
  cacheDirectory: join(__dirname, 'chrome'),
};
