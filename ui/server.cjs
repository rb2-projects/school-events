const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const MIME_TYPES = {
    '.html': 'text/html',
    '.js': 'text/javascript',
    '.css': 'text/css',
    '.json': 'application/json',
    '.png': 'image/png',
    '.jpg': 'image/jpg',
    '.gif': 'image/gif',
    '.svg': 'image/svg+xml',
};

http.createServer((req, res) => {
    const urlPath = req.url.split('?')[0];
    console.log(`${new Date().toISOString()} - ${req.method} ${req.url}`);

    let filePath = '.' + urlPath;
    if (filePath === './') filePath = './index.html';

    // Support files in public/ as well for the UI structure
    const publicPath = './public' + urlPath;

    const tryServe = (p) => {
        const extname = String(path.extname(p)).toLowerCase();
        const contentType = MIME_TYPES[extname] || 'application/octet-stream';

        fs.readFile(p, (error, content) => {
            if (error) {
                if (error.code === 'ENOENT') {
                    if (p.startsWith('./public')) {
                        console.error(`404 - Not Found: ${p}`);
                        res.writeHead(404);
                        res.end('Not found');
                    } else {
                        tryServe(publicPath);
                    }
                } else {
                    console.error(`500 - Error: ${error.code} for ${p}`);
                    res.writeHead(500);
                    res.end('Server Error: ' + error.code);
                }
            } else {
                res.writeHead(200, { 'Content-Type': contentType });
                res.end(content, 'utf-8');
            }
        });
    };

    tryServe(filePath);

}).listen(PORT);

console.log(`Server running at http://localhost:${PORT}/`);
