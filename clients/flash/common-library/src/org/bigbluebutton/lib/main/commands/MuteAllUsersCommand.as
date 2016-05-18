package org.bigbluebutton.lib.main.commands {
	import org.bigbluebutton.lib.user.services.IUsersService;
	
	import robotlegs.bender.bundles.mvcs.Command;
	
	public class MuteAllUsersCommand extends Command {
		
		[Inject]
		public var userService:IUsersService;
		
		[Inject]
		public var mute:Boolean;
		
		override public function execute():void {
			trace("MuteAllUsersCommand.execute()");
			userService.muteAllUsers(mute);
		}
	}
}
