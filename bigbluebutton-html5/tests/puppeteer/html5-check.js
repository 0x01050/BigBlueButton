require('dotenv').config();
const axios = require('axios');
const path = require('path');
const url = require('url');
const helper = require('./helper');
const httpPath = path.join(path.dirname(require.resolve('axios')),'lib/adapters/http');
const http = require(httpPath);

(async () =>
{
  var bbb = url.parse(process.env.BBB_SERVER_URL)
  var check = bbb.protocol + "//" + bbb.hostname + "/html5client/check";
  console.log("HTML5 check URL: " + check);
  const maxRetries = 20;
  const retryDelay = 10000;
  var retryCount = 0;
  while(true)
  {
    try
    {
      var response = await axios.get(check, {adapter: http});
      var status = response.data.html5clientStatus
      console.log(response.data);
      if(status === 'running')
      {
        break;
      }
      else if (retryCount < maxRetries)
      {
        retryCount++;
      }
      else
      {
        console.log("Too many attempts. Exiting...");
        process.exit(1);
      }
    }
    catch(e)
    {
      console.log(e.message);
      if (retryCount < maxRetries)
      {
        retryCount++;
      }
      else
      {
        console.log("Too many attempts. Exiting...");
        process.exit(1);
      }
    }
    console.log("Retrying (attempt " + (retryCount) + "/" + maxRetries + ")...");
    await helper.sleep(retryDelay);
  }
})();