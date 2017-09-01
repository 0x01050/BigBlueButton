define([
  '../core',

  '../event',
  './trigger',
], (jQuery) => {
  jQuery.each(('blur focus focusin focusout load resize scroll unload click dblclick ' +
	'mousedown mouseup mousemove mouseover mouseout mouseenter mouseleave ' +
	'change select submit keydown keypress keyup error contextmenu').split(' '),
	(i, name) => {
	// Handle event binding
  jQuery.fn[name] = function (data, fn) {
    return arguments.length > 0 ?
			this.on(name, null, data, fn) :
			this.trigger(name);
  };
});

  jQuery.fn.extend({
    hover(fnOver, fnOut) {
      return this.mouseenter(fnOver).mouseleave(fnOut || fnOver);
    },
  });
});
