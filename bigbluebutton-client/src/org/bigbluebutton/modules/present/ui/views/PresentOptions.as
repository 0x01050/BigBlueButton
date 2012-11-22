package org.bigbluebutton.modules.present.ui.views
{
	import org.bigbluebutton.core.BBB;
	import org.bigbluebutton.common.LogUtil;
	public class PresentOptions
	{
		[Bindable] public var showWindowControls:Boolean = true;
		[Bindable] public var baseTabIndex:int;
		
		public function PresentOptions()
		{
			var vxml:XML = BBB.getConfigForModule("PresentModule");
			if (vxml != null) {
				if (vxml.@showWindowControls != undefined) {
					showWindowControls = (vxml.@showWindowControls.toString().toUpperCase() == "TRUE") ? true : false;
				}
				if (vxml.@baseTabIndex != undefined) {
					baseTabIndex = vxml.@baseTabIndex;
				}
				else{
					baseTabIndex = 401;
				}
			}
		}
	}
}