import React, { Component, PropTypes } from 'react';
import { showModal } from '/imports/ui/components/app/service';
import Audio from '/imports/ui/components/audio-modal/component';
import Button from '/imports/ui/components/button/component';
import styles from './styles.scss';
import EmojiContainer from './emoji-menu/container';
import ActionsDropdown from './actions-dropdown/component';
import Auth from '/imports/ui/services/auth/index';
import Users from '/imports/api/users/index';
import JoinAudioOptionsContainer from './audio-menu/container';
import MuteAudioContainer from './mute-button/container';
import JoinVideo from './video-button/component';

export default class ActionsBar extends Component {
  constructor(props) {
    super(props);
  }

  openJoinAudio() {
    return showModal(<Audio handleJoinListenOnly={this.props.handleJoinListenOnly} />)
  }

  renderForPresenter() {
    return (
      <div className={styles.actionsbar}>
        <div className={styles.left}>
          <ActionsDropdown />
        </div>
        <div className={styles.center}>
          <MuteAudioContainer />
          <JoinAudioOptionsContainer
            handleJoinAudio={this.openJoinAudio.bind(this)}
            handleCloseAudio={this.props.handleExitAudio}

          />
          {/*<JoinVideo />*/}
          <EmojiContainer />
        </div>
        <div className={styles.hidden}>
          <ActionsDropdown />
        </div>
      </div>
    );
  }

  renderForUser() {
    return (
      <div className={styles.actionsbar}>
        <div className={styles.center}>
          <MuteAudioContainer />
          <JoinAudioOptionsContainer
            handleJoinAudio={this.openJoinAudio.bind(this)}
            handleCloseAudio={this.props.handleExitAudio}

          />
          {/*<JoinVideo />*/}
          <EmojiContainer />
        </div>
      </div>
    );
  }

  render() {
    const { isUserPresenter } = this.props;

    return isUserPresenter ?
      this.renderForPresenter() :
      this.renderForUser();
  }
}
