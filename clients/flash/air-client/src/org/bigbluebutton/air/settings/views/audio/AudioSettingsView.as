package org.bigbluebutton.air.settings.views.audio {
	import org.bigbluebutton.air.common.views.NoTabView;
	import org.bigbluebutton.air.main.views.TopToolbarAIR;
	import org.bigbluebutton.air.settings.views.TopToolbarSubSettings;
	
	import spark.layouts.VerticalLayout;
	
	public class AudioSettingsView extends NoTabView {
		
		private var _settingsView:AudioSettingsViewBase;
		
		public function AudioSettingsView() {
			super();
			
			var vLayout:VerticalLayout = new VerticalLayout();
			vLayout.gap = 0;
			layout = vLayout;
			
			_settingsView = new AudioSettingsViewBaseAIR();
			_settingsView.percentHeight = 100;
			_settingsView.percentWidth = 100;
			addElement(_settingsView);
		}
		
		override protected function createToolbar():TopToolbarAIR {
			return new TopToolbarSubSettings();
		}
	}
}
