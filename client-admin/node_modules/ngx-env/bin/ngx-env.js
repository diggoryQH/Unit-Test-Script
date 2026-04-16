#!/usr/bin/env node
var path = require('path')
var argv = require('minimist')(process.argv.slice(2));
var ngEnv = require('../lib')

var outPath = argv.outPath || path.join(path.resolve('.'), 'src', 'environments')
var outFile = argv.outFile || 'environment.ts'

var params = {}
Object.keys(argv).forEach(function (key) {
  if (key.charAt(0) === 'e') {
    params[key.substring(1)] = argv[key]
  }
})

var json = require(path.resolve(argv.template))(ngEnv.env, params)
ngEnv.writeEnv(json, path.join(outPath, outFile))



