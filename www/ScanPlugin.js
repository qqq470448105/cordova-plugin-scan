var exec = require('cordova/exec');

var ScanPlugin = {
  scan: function (successFn, failureFn) {
    exec(successFn, failureFn, 'ScanPlugin', 'scan');
  }
}

module.exports = ScanPlugin;