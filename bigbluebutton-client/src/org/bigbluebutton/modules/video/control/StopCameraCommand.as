package org.bigbluebutton.modules.video.control
{
	import org.puremvc.as3.multicore.interfaces.ICommand;
	import org.puremvc.as3.multicore.patterns.command.SimpleCommand;
	import org.puremvc.as3.multicore.interfaces.INotification;

	public class StopCameraCommand extends SimpleCommand implements ICommand
	{
		public function StopCameraCommand()
		{
			super();
		}
		
		override public function execute(notification:INotification):void
		{
		}
		
	}
}