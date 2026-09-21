import fs from 'fs';

async function test() {
  const shortcode = "Dcg31M0zTDP";
  const url = `https://www.instagram.com/p/${shortcode}/embed/captioned/`;
  const res = await fetch(url, {
    headers: {
      "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
      "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
      "Accept-Language": "en-US,en;q=0.9"
    }
  });
  console.log("Status:", res.status);
  const text = await res.text();
  fs.writeFileSync("/tmp/ig_embed.html", text);
  console.log("Saved HTML, length:", text.length);
}
test();
