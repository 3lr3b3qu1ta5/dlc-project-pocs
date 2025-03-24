// Framework function to make a REST API request via the running device
function callPublicRESTApi(url){
  return new Promise(resolve => {
    window.valueCaptured = null;
    device.callPublicRESTApi(url);
    let attempts = 0;
    let intervalCheck = setInterval(() => {
      if (window.valueCaptured !== null || attempts >= 20) {
        clearInterval(intervalCheck);
        intervalCheck = null;
        resolve(attempts >= 20 ? "timeout !" : JSON.parse(window.valueCaptured));
      }
      attempts++;
    }, 500);
  });
}

// Framework function to publish the result of the computation
function publishResult(result) {
  window.jsResult = result;
}

// Your code starts here...

function countChar(text, char) {
  let count = 0;
  for (let i = 0; i < text.length; i++) {
      if (text[i] == char) {
          count++;
      }
  }
  return count;
}

async function main() {
  let result = await callPublicRESTApi("https://jsonplaceholder.typicode.com/posts/1");
  let totalChars = countChar(result.body, "a");
  publishResult("The body field contains " + totalChars + " times the character 'a'");
}

main();
