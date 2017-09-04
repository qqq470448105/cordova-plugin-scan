var exec = require('cordova/exec');

var ScanPlugin = {
  scan: function (param, successFn, failureFn) {
    exec(successFn, failureFn, 'ScanPlugin', 'scan', [param]);
  }
}

module.exports = ScanPlugin;