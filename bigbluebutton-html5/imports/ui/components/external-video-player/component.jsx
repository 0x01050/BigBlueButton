import React, { Component } from 'react';
import injectWbResizeEvent from '/imports/ui/components/presentation/resize-wrapper/component';
import { defineMessages, injectIntl } from 'react-intl';
import ReactPlayer from 'react-player';
import { sendMessage, onMessage, removeAllListeners } from './service';
import logger from '/imports/startup/client/logger';

import ArcPlayer from './custom-players/arc-player';

import { styles } from './styles';

const intlMessages = defineMessages({
  autoPlayWarning: {
    id: 'app.externalVideo.autoPlayWarning',
    description: 'Shown when user needs to interact with player to make it work',
  },
});

const SYNC_INTERVAL_SECONDS = 2;
const AUTO_PLAY_BLOCK_DETECTION_TIMEOUT_SECONDS = 5;

ReactPlayer.addCustomPlayer(ArcPlayer);

class VideoPlayer extends Component {
  constructor(props) {
    super(props);

    const { isPresenter } = props;

    this.player = null;
    this.syncInterval = null;
    this.autoPlayTimeout = null;
    this.state = {
      mutedByEchoTest: false,
      playing: false,
      hasPlayedBefore: false,
      autoPlayBlocked: false,
      playbackRate: 1,
    };

    this.opts = {
      controls: isPresenter,
      youtube: {
        playerVars: {
          autoplay: 1,
          modestbranding: 1,
          autohide: 1,
          rel: 0,
          ecver: 2,
          controls: isPresenter ? 1 : 2,
        },
      },
      preload: true,
    };

    this.registerVideoListeners = this.registerVideoListeners.bind(this);
    this.autoPlayBlockDetected = this.autoPlayBlockDetected.bind(this);
    this.clearVideoListeners = this.clearVideoListeners.bind(this);
    this.handleResize = this.handleResize.bind(this);
    this.handleOnReady = this.handleOnReady.bind(this);
    this.handleOnPlay = this.handleOnPlay.bind(this);
    this.handleOnPause = this.handleOnPause.bind(this);
    this.resizeListener = () => {
      setTimeout(this.handleResize, 0);
    };
  }

  componentDidMount() {
    window.addEventListener('resize', this.resizeListener);

    clearInterval(this.syncInterval);
    this.registerVideoListeners();
  }

  componentWillUnmount() {
    window.removeEventListener('resize', this.resizeListener);
    this.clearVideoListeners();

    clearInterval(this.syncInterval);
    clearTimeout(this.autoPlayTimeout);
    this.player = null;
  }

  componentDidUpdate(prevProp, prevState) {
    // Detect presenter change and redo the sync and listeners to reassign video to the new one
    if (this.props.isPresenter !== prevProp.isPresenter) {
      this.clearVideoListeners();
      clearInterval(this.syncInterval);
      this.registerVideoListeners();
    }
  }

  static getDerivedStateFromProps(props) {
    const { inEchoTest } = props;

    return { mutedByEchoTest: inEchoTest };
  }

  autoPlayBlockDetected() {
    this.setState({autoPlayBlocked: true});
  }

  getCurrentPlaybackRate() {
    const intPlayer = this.player.getInternalPlayer();

    return (intPlayer && intPlayer.getPlaybackRate && intPlayer.getPlaybackRate()) || 1;
  }

  handleResize() {
    if (!this.player || !this.playerParent) {
      return;
    }

    const par = this.playerParent.parentElement;
    const w = par.clientWidth;
    const h = par.clientHeight;
    const idealW = h * 16 / 9;

    const style = {};
    if (idealW > w) {
      style.width = w;
      style.height = w * 9 / 16;
    } else {
      style.width = idealW;
      style.height = h;
    }

    const styleStr = `width: ${style.width}px; height: ${style.height}px;`;
    this.player.wrapper.style = styleStr;
    this.playerParent.style = styleStr;
  }

  clearVideoListeners() {
    removeAllListeners('play');
    removeAllListeners('stop');
    removeAllListeners('playerUpdate');
  }

  registerVideoListeners() {
    const { isPresenter } = this.props;

    if (isPresenter) {
      this.syncInterval = setInterval(() => {
        const curTime = this.player.getCurrentTime();
        const rate = this.getCurrentPlaybackRate();

        sendMessage('playerUpdate', { rate, time: curTime, state: this.state.playing });
      }, SYNC_INTERVAL_SECONDS * 1000);
    } else {
      onMessage('play', ({ time }) => {
        if (!this.player || !this.state.hasPlayedBefore) {
          return;
        }

        this.player.seekTo(time);
        this.setState({ playing: true });

        logger.debug({ logCode: 'external_video_client_play' }, 'Play external video');
      });

      onMessage('stop', ({ time }) => {
        if (!this.player || !this.state.hasPlayedBefore) {
          return;
        }
        this.player.seekTo(time);
        this.setState({ playing: false });

        logger.debug({ logCode: 'external_video_client_stop' }, 'Stop external video');
      });

      onMessage('playerUpdate', (data) => {
        if (!this.player || !this.state.hasPlayedBefore) {
          return;
        }

        if (data.rate !== this.player.props.playbackRate) {
          this.setState({ playbackRate: data.rate });
          logger.debug({
            logCode: 'external_video_client_update_rate',
            extraInfo: {
              newRate: data.rate,
            },
          }, 'Change external video playback rate.');
        }

        if (Math.abs(this.player.getCurrentTime() - data.time) > SYNC_INTERVAL_SECONDS) {
          this.player.seekTo(data.time, true);
          logger.debug({
            logCode: 'external_video_client_update_seek',
            extraInfo: {
              time: data.time,
            },
          }, 'Seek external video to:');
        }

        if (this.state.playing !== data.state) {
          this.setState({ playing: data.state });
        }
      });
    }
  }

  handleOnReady() {
    const { isPresenter } = this.props;

    if (!isPresenter) {
      sendMessage('viewerJoined');
    }

    this.handleResize();

    this.autoPlayTimeout = setTimeout(this.autoPlayBlockDetected, AUTO_PLAY_BLOCK_DETECTION_TIMEOUT_SECONDS * 1000);

    this.setState({ playing: true });
  }

  handleOnPlay() {
    const { isPresenter } = this.props;
    const curTime = this.player.getCurrentTime();

    if (isPresenter) {
      sendMessage('play', { time: curTime });
    }
    this.setState({ playing: true });

    if (!this.state.hasPlayedBefore) {
      this.setState({ hasPlayedBefore: true, autoPlayBlocked: false });
      clearTimeout(this.autoPlayTimeout);
    }
  }

  handleOnPause() {
    const { isPresenter } = this.props;
    const curTime = this.player.getCurrentTime();

    if (isPresenter) {
      sendMessage('stop', { time: curTime });
    }
    this.setState({ playing: false });

    if (!this.state.hasPlayedBefore) {
      this.setState({ hasPlayedBefore: true, autoPlayBlocked: false });
      clearTimeout(this.autoPlayTimeout);
    }
  }

  render() {
    const { videoUrl, intl } = this.props;
    const { playing, playbackRate, mutedByEchoTest, autoPlayBlocked } = this.state;

    return (
      <div
        id="video-player"
        data-test="videoPlayer"
        ref={(ref) => { this.playerParent = ref; }}
      >
        {autoPlayBlocked ?
          <p className={styles.autoPlayWarning}>
            {intl.formatMessage(intlMessages.autoPlayWarning)}
          </p>
          : ''
        }
        <ReactPlayer
          className={styles.videoPlayer}
          url={videoUrl}
          config={this.opts}
          muted={mutedByEchoTest}
          playing={playing}
          playbackRate={playbackRate}
          onReady={this.handleOnReady}
          onPlay={this.handleOnPlay}
          onPause={this.handleOnPause}
          ref={(ref) => { this.player = ref; }}
        />
      </div>
    );
  }
}

export default injectIntl(injectWbResizeEvent(VideoPlayer));
