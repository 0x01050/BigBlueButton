import RedisPubSub from '/imports/startup/server/redis';
import handleGetUsers from './handlers/getUsers';
import handleRemoveUser from './handlers/removeUser';
import handlePresenterAssigned from './handlers/presenterAssigned';
import handleEmojiStatus from './handlers/emojiStatus';
import handleLockedStatusChange from './handlers/lockedStatusChange';
import handleUserJoined from './handlers/userJoined';
import handleValidateAuthToken from './handlers/validateAuthToken';
import handleVoiceUpdate from './handlers/voiceUpdate';
import handleListeningOnly from './handlers/listeningOnly';

RedisPubSub.on('validate_auth_token_reply', handleValidateAuthToken);
RedisPubSub.on('get_users_reply', handleGetUsers);
RedisPubSub.on('user_joined_message', handleUserJoined);
RedisPubSub.on('user_eject_from_meeting', handleRemoveUser);
RedisPubSub.on('disconnect_user_message', handleRemoveUser);
RedisPubSub.on('user_left_message', handleRemoveUser);
RedisPubSub.on('presenter_assigned_message', handlePresenterAssigned);
RedisPubSub.on('user_emoji_status_message', handleEmojiStatus);
RedisPubSub.on('user_locked_message', handleLockedStatusChange);
RedisPubSub.on('user_unlocked_message', handleLockedStatusChange);
RedisPubSub.on('user_left_voice_message', handleVoiceUpdate);
RedisPubSub.on('user_joined_voice_message', handleVoiceUpdate);
RedisPubSub.on('user_voice_talking_message', handleVoiceUpdate);
RedisPubSub.on('user_voice_muted_message', handleVoiceUpdate);
RedisPubSub.on('user_listening_only', handleListeningOnly);
