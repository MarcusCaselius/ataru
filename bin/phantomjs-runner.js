var page = require('webpage').create();
var system = require('system');
var args = system.args;

console.log("running browser tests with cookie value", args[1]);

phantom.addCookie({
  'name': 'ring-session',
  'value': args[1],
  'domain': 'localhost'
});

global.testsSuccessful = undefined;
var url = 'http://localhost:8350/lomake-editori/test.html';
var resultPrefix = '*** TEST';
var successMsg = ' SUCCESS';
var failMsg = ' FAIL';
var timeoutMs = 10 * 60 * 1000;
var startTime = new Date().getTime();

function startsWith(haystack, needle) {
  return haystack.substring(0, needle.length) === needle
}

page.onConsoleMessage = function (message) {
  console.log(message);
  if (startsWith(message, resultPrefix)) {
    if (startsWith(message, resultPrefix + successMsg)) {
      global.testsSuccessful = true;
    } else if (startsWith(message, resultPrefix + failMsg)) {
      global.testsSuccessful = false;
    } else {
      console.log("Unknown result:", message);
      global.testsSuccessful = false;
    }
  }
};

page.open(url, function (status) {
  if (status !== "success") {
    console.log('Failed to open');
    phantom.exit(1);
  }

  function stopWhenFinished() {
    if (new Date().getTime() > startTime + timeoutMs) {
      console.log('Tests timed out after', timeoutMs);
      phantom.exit(2);
    } else if (typeof global.testsSuccessful === 'undefined') {
      setTimeout(stopWhenFinished, 1000);
    } else {
      phantom.exit(global.testsSuccessful ? 0 : 1);
    }
  }

  page.evaluate(function() {
    if (typeof ataru !== 'undefined' && !!ataru) {
      ataru.virkailija.browser_runner.run();
    } else {
      console.error("Error loading script");
      phantom.exit(3);
    }

  });

  stopWhenFinished();
});