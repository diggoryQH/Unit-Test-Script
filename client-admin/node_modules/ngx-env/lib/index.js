const fs = require('fs');
const path = require('path');
const stringifyObject = require('stringify-object');
const prettyjson = require('prettyjson');
const mkdirp = require('mkdirp')
require('node-env-file')(path.join(path.resolve('.'), '.env'), {overwrite: true, raise: false});

const env = process.env

function writeEnv(json, ngEnvPath) {
  mkdirp.sync(path.dirname(ngEnvPath))
  fs.writeFileSync(ngEnvPath, `export const environment = ${stringifyObject(json, {indent: '  '})};\n`);
  console.log('created', ngEnvPath);
  console.log(prettyjson.render(json))
}

module.exports = {
  writeEnv, env
}