import React, { Component } from 'react';
import PropTypes from 'prop-types';
import cx from 'classnames';
import { defineMessages, intlShape, injectIntl } from 'react-intl';
import Button from '/imports/ui/components/button/component';
import getFromUserSettings from '/imports/ui/services/users-settings';
import withShortcutHelper from '/imports/ui/components/shortcut-help/service';
import { styles } from './styles';

const intlMessages = defineMessages({
  joinAudio: {
    id: 'app.audio.joinAudio',
    description: 'Join audio button label',
  },
  leaveAudio: {
    id: 'app.audio.leaveAudio',
    description: 'Leave audio button label',
  },
  muteAudio: {
    id: 'app.actionsBar.muteLabel',
    description: 'Mute audio button label',
  },
  unmuteAudio: {
    id: 'app.actionsBar.unmuteLabel',
    description: 'Unmute audio button label',
  },
});

const propTypes = {
  handleToggleMuteMicrophone: PropTypes.func.isRequired,
  handleJoinAudio: PropTypes.func.isRequired,
  handleLeaveAudio: PropTypes.func.isRequired,
  disable: PropTypes.bool.isRequired,
  unmute: PropTypes.bool,
  mute: PropTypes.bool.isRequired,
  join: PropTypes.bool.isRequired,
  intl: intlShape.isRequired,
  glow: PropTypes.bool,
};

const defaultProps = {
  glow: false,
  unmute: false,
};

class AudioControls extends Component {
  componentDidMount() {
    if (Meteor.settings.public.allowOutsideCommands.toggleSelfVoice ||
      getFromUserSettings('outsideToggleSelfVoice', false)) {
      window.addEventListener('message', this.props.processToggleMuteFromOutside);
    }
  }

  render() {
    const {
      handleToggleMuteMicrophone,
      handleJoinAudio,
      handleLeaveAudio,
      mute,
      unmute,
      disable,
      glow,
      join,
      intl,
      shortcuts,
    } = this.props;

    return (
      <span className={styles.container}>
        {mute ?
          <Button
            className={glow ? cx(styles.button, styles.glow) : styles.button}
            onClick={handleToggleMuteMicrophone}
            disabled={disable}
            hideLabel
            label={unmute ? intl.formatMessage(intlMessages.unmuteAudio) :
              intl.formatMessage(intlMessages.muteAudio)}
            aria-label={unmute ? intl.formatMessage(intlMessages.unmuteAudio) :
              intl.formatMessage(intlMessages.muteAudio)}
            color="primary"
            icon={unmute ? 'mute' : 'unmute'}
            size="lg"
            circle
            accessKey={shortcuts.toggleMute}
          /> : null}
        <Button
          className={styles.button}
          onClick={join ? handleLeaveAudio : handleJoinAudio}
          disabled={disable}
          hideLabel
          aria-label={join ? intl.formatMessage(intlMessages.leaveAudio) :
            intl.formatMessage(intlMessages.joinAudio)}
          label={join ? intl.formatMessage(intlMessages.leaveAudio) :
            intl.formatMessage(intlMessages.joinAudio)}
          color={join ? 'danger' : 'primary'}
          icon={join ? 'audio_off' : 'audio_on'}
          size="lg"
          circle
          accessKey={join ? shortcuts.leaveAudio : shortcuts.joinAudio}
        />
      </span>);
  }
}

AudioControls.propTypes = propTypes;
AudioControls.defaultProps = defaultProps;

export default withShortcutHelper(injectIntl(AudioControls), ['joinAudio', 'leaveAudio', 'toggleMute']);
