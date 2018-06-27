import React from 'react';
import { withTracker } from 'meteor/react-meteor-data';
import { withModalMounter } from '/imports/ui/components/modal/service';
import { injectIntl, defineMessages } from 'react-intl';
import Breakouts from '/imports/api/breakouts';
import Service from './service';
import AudioModalContainer from './audio-modal/container';

const intlMessages = defineMessages({
  joinedAudio: {
    id: 'app.audioManager.joinedAudio',
    description: 'Joined audio toast message',
  },
  joinedEcho: {
    id: 'app.audioManager.joinedEcho',
    description: 'Joined echo test toast message',
  },
  leftAudio: {
    id: 'app.audioManager.leftAudio',
    description: 'Left audio toast message',
  },
  genericError: {
    id: 'app.audioManager.genericError',
    description: 'Generic error message',
  },
  connectionError: {
    id: 'app.audioManager.connectionError',
    description: 'Connection error message',
  },
  requestTimeout: {
    id: 'app.audioManager.requestTimeout',
    description: 'Request timeout error message',
  },
  invalidTarget: {
    id: 'app.audioManager.invalidTarget',
    description: 'Invalid target error message',
  },
  mediaError: {
    id: 'app.audioManager.mediaError',
    description: 'Media error message',
  },
});


class AudioContainer extends React.Component {
  constructor(props) {
    super(props);

    this.init = props.init.bind(this);
  }

  componentDidMount() {
    this.init();
  }

  render() {
    return null;
  }
}

let didMountAutoJoin = false;

export default withModalMounter(injectIntl(withTracker(({ mountModal, intl }) => {
  const APP_CONFIG = Meteor.settings.public.app;

  const { autoJoin } = APP_CONFIG;
  const openAudioModal = mountModal.bind(
    null,
    <AudioModalContainer />,
  );

  Breakouts.find().observeChanges({
    removed() {
      // if the user joined a breakout room, the main room's audio was
      // programmatically dropped to avoid interference. On breakout end,
      // offer to rejoin main room audio only if the user is not in audio already
      if (Service.isUsingAudio()) {
        return;
      }
      setTimeout(() => openAudioModal(), 0);
    },
  });

  const messages = {
    info: {
      JOINED_AUDIO: intl.formatMessage(intlMessages.joinedAudio),
      JOINED_ECHO: intl.formatMessage(intlMessages.joinedEcho),
      LEFT_AUDIO: intl.formatMessage(intlMessages.leftAudio),
    },
    error: {
      GENERIC_ERROR: intl.formatMessage(intlMessages.genericError),
      CONNECTION_ERROR: intl.formatMessage(intlMessages.connectionError),
      REQUEST_TIMEOUT: intl.formatMessage(intlMessages.requestTimeout),
      INVALID_TARGET: intl.formatMessage(intlMessages.invalidTarget),
      MEDIA_ERROR: intl.formatMessage(intlMessages.mediaError),
    },
  };

  return {
    init: () => {
      Service.init(messages);
      Service.changeOutputDevice(document.querySelector('#remote-media').sinkId);
      if (!autoJoin || didMountAutoJoin) return;
      openAudioModal();
      didMountAutoJoin = true;
    },
  };
})(AudioContainer)));
