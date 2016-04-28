package org.bigbluebutton.web.main.views {
	import mx.graphics.SolidColor;
	
	import org.bigbluebutton.lib.main.views.MenuButtonsBase;
	import org.bigbluebutton.lib.presentation.models.Presentation;
	import org.bigbluebutton.lib.presentation.views.PresentationViewBase;
	
	import spark.components.Group;
	import spark.layouts.VerticalLayout;
	import spark.primitives.Rect;
	
	public class MainPanel extends Group {
		private var _presentationView:PresentationViewBase;
		private var _menuButtons:MenuButtonsBase;
		
		public function MainPanel() {
			super();
			
			var l:VerticalLayout = new VerticalLayout();
			l.gap = 0;
			l.horizontalAlign = "center";
			layout = l;
			
			_presentationView = new PresentationViewBase();
			_presentationView.percentWidth = 100;
			_presentationView.percentHeight = 100;
			addElement(_presentationView);
			
			_menuButtons = new MenuButtonsBase();
			addElement(_menuButtons);
		}
		
		override protected function updateDisplayList(w:Number, h:Number):void {
			super.updateDisplayList(w, h);
			
			_presentationView.width = w;
			_presentationView.height = h - _menuButtons.height;
		}
	}
}