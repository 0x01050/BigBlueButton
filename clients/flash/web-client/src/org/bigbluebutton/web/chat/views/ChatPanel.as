package org.bigbluebutton.web.chat.views {
	import mx.graphics.SolidColor;
	
	import org.bigbluebutton.web.common.views.IPanelAdjustable;
	
	import spark.components.Group;
	import spark.primitives.Rect;
	
	public class ChatPanel extends Group implements IPanelAdjustable {
		private var _adjustable:Boolean = false;
		
		public function set adjustable(v:Boolean):void {
			_adjustable = v;
		}
		
		public function get adjustable():Boolean {
			return _adjustable;
		}
		
		public function ChatPanel() {
			super();
			
			var fillerRect:Rect = new Rect();
			fillerRect.percentWidth = 100;
			fillerRect.percentHeight = 100;
			var fill:SolidColor = new SolidColor();
			fill.color = 0x00FF00;
			fillerRect.fill = fill;
			addElement(fillerRect);
			
			
		}
	}
}