const Page = require('./page');
const Send = require('./chat/send');
const Clear = require('./chat/clear');
const Copy = require('./chat/copy');

describe('Chat', () => {

  test('Send message', async () => {
    const test = new Send();
    let response;
    try {
      await test.init(Page.getArgs());
      response = await test.test();
    } catch (e) {
      console.log(e);
    } finally {
      await test.close();
    }
    expect(response).toBe(true);
  });

  test('Clear chat', async () => {
    const test = new Clear();
    let response;
    try {
      await test.init(Page.getArgs());
      response = await test.test();
    } catch (e) {
      console.log(e);
    } finally {
      await test.close();
    }
    expect(response).toBe(true);
  });

  test('Copy chat', async () => {
    const test = new Copy();
    let response;
    try {
      await test.init(Page.getArgs());
      response = await test.test();
    } catch (e) {
      console.log(e);
    } finally {
      await test.close();
    }
    expect(response).toBe(true);
  });

});
