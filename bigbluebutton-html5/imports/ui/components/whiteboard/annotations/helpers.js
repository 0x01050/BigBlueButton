const colourToHex = (value) => {
  let hex;
  hex = parseInt(value, 10).toString(16);
  while (hex.length < 6) {
    hex = `0${hex}`;
  }

  return `#${hex}`;
};

const formatColor = (color) => {
  let _color;

  if (!color) {
    _color = '0'; // default value
  } else {
    _color = color;
  }

  if (!_color.toString().match(/#.*/)) {
    _color = colourToHex(_color);
  }

  return _color;
};

const getStrokeWidth = (thickness, slideWidth) => (thickness * slideWidth) / 100;

export default {
  formatColor,
  getStrokeWidth,
};
