const Page = require('./page');
const helper = require('./helper');
const e = require('./elements');

class DrawTestPage extends Page {
  constructor() {
    super('whiteboard-draw');
  }

  async test() {
    await this.click(e.tools);
    await this.click(e.rectangle);
    await this.page.waitFor(e.whiteboard);

    const shapes0 = await this.getTestElements();

    const wb = await this.page.$(e.whiteboard);
    const wbBox = await wb.boundingBox();
    await this.page.mouse.move(wbBox.x + 0.3 * wbBox.width, wbBox.y + 0.3 * wbBox.height);
    await this.page.mouse.down();
    await this.page.mouse.move(wbBox.x + 0.7 * wbBox.width, wbBox.y + 0.7 * wbBox.height);
    await this.page.mouse.up();

    await this.screenshot(true);
    const shapes1 = await this.getTestElements();

    console.log('\nShapes before drawing box:');
    console.log(shapes0);
    console.log('\nShapes after drawing box:');
    console.log(shapes1);
  }

  async getTestElements() {
    const shapes = await this.page.evaluate(() => document.querySelector('svg g[clip-path]').children[1].outerHTML);
    return shapes;
  }
}

module.exports = exports = DrawTestPage;
