package org.bigbluebutton.air.chat.models {
	
	import mx.collections.ArrayCollection;
	
	import spark.collections.Sort;
	import spark.collections.SortField;
	
	import org.as3commons.lang.StringUtils;
	import org.bigbluebutton.air.chat.commands.RequestGroupChatHistorySignal;
	import org.bigbluebutton.air.chat.commands.RequestWelcomeMessageSignal;
	import org.bigbluebutton.air.main.models.IMeetingData;
	import org.osflash.signals.Signal;
	
	public class ChatMessagesSession implements IChatMessagesSession {
		
		private static const DEFAULT_CHAT_ID:String = "MAIN-PUBLIC-GROUP-CHAT";
		
		[Inject]
		public var meetingData:IMeetingData;
		
		[Inject]
		public var requestChatHistorySignal:RequestGroupChatHistorySignal;
		
		[Inject]
		public var requestWelcomeMessageSignal:RequestWelcomeMessageSignal;
		
		private var _groupChatChangeSignal:Signal = new Signal();
		
		[Bindable]
		public var chats:ArrayCollection;
		
		public function ChatMessagesSession():void {
			chats = new ArrayCollection();
		}
		
		public function get groupChatChangeSignal():Signal {
			return _groupChatChangeSignal;
		}
		
		private function sortChats():void {
			if (!chats.sort) {
				var sort:Sort = new Sort();
				sort.fields = [new SortField("isPublic", true), new SortField("name", false)];
				chats.sort = sort;
			}
			chats.refresh();
		}
		
		public function getGroupByChatId(chatId:String):GroupChat {
			for each (var chat:GroupChat in chats) {
				if (chat.chatId == chatId) {
					return chat;
				}
			}
			
			return null;
		}
		
		public function getGroupByUserId(userId:String):GroupChat {
			for each (var chat:GroupChat in chats) {
				if (chat.partnerId == userId) {
					return chat;
				}
			}
			
			return null;
		}
		
		public function addGroupChatsList(chatVOs:Array):void {
			for each (var chat:GroupChatVO in chatVOs) {
				chats.addItem(convertGroupChatVO(chat));
				if (chat.id == DEFAULT_CHAT_ID) {
					requestWelcomeMessageSignal.dispatch();
				}
				requestChatHistorySignal.dispatch(chat.id);
			}
			sortChats();
		}
		
		public function addMessageHistory(chatId:String, messages:Array):void {
			var chat:GroupChat = getGroupByChatId(chatId);
			if (chat) {
				chat.addChatHistory(messages);
			}
		}
		
		public function clearPublicChat(chatId:String):void {
			var chatGroup:GroupChat = getGroupByChatId(chatId);
			if (chatGroup) {
				chatGroup.clearMessages();
				if (chatId == DEFAULT_CHAT_ID) {
					requestWelcomeMessageSignal.dispatch();
				}
			}
		}
		
		public function addChatMessage(chatId:String, newMessage:ChatMessageVO):void {
			var chatGroup:GroupChat = getGroupByChatId(chatId);
			if (chatGroup) {
				chatGroup.newChatMessage(newMessage);
			}
		}
		
		public function addGroupChat(vo:GroupChatVO):void {
			chats.addItem(convertGroupChatVO(vo));
			sortChats();
			_groupChatChangeSignal.dispatch(vo, GroupChatChangeEnum.ADD);
		}
		
		private function convertGroupChatVO(vo:GroupChatVO):GroupChat {
			var partnerId:String = "";
			
			if (vo.access == GroupChat.PRIVATE) {
				var myUserId:String = meetingData.users.me.intId;
				for each (var user:GroupChatUser in vo.users) {
					if (user.id != myUserId) {
						partnerId = user.id;
						// The name of a private chat group is supposed to be who you're chatting
						// with, but it comes in relative to who created it so we need to fix the name.
						vo.name = user.name;
					}
				}
			}
			
			// Need to replace the name with a more human redable version
			if (vo.id == DEFAULT_CHAT_ID) {
				vo.name = "Public Chat";
			}
			
			var partnerRole:String;
			if (!StringUtils.isEmpty(partnerId) && meetingData.users.getUser(partnerId)) {
				partnerRole = meetingData.users.getUser(partnerId).role;
			}
			
			var newGroupChat:GroupChat = new GroupChat(vo.id, vo.name, vo.access == GroupChat.PUBLIC, partnerId, partnerRole);
			
			return newGroupChat;
		}
	}
}
