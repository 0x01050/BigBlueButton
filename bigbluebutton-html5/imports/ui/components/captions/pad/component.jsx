import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Session } from 'meteor/session';
import { defineMessages, injectIntl } from 'react-intl';
import injectWbResizeEvent from '/imports/ui/components/presentation/resize-wrapper/component';
import Button from '/imports/ui/components/button/component';
import PadService from './service';
import CaptionsService from '/imports/ui/components/captions/service';
import { styles } from './styles';

const intlMessages = defineMessages({
  hide: {
    id: 'app.captions.pad.hide',
    description: 'Label for hiding closed captions pad',
  },
  tip: {
    id: 'app.captions.pad.tip',
    description: 'Label for tip on how to escape closed captions iframe',
  },
  takeOwnership: {
    id: 'app.captions.pad.ownership',
    description: 'Label for taking ownership of closed captions pad',
  },
  dictationStart: {
    id: 'app.captions.pad.dictationStart',
    description: 'Label for starting speech recognition',
  },
  dictationStop: {
    id: 'app.captions.pad.dictationStop',
    description: 'Label for stoping speech recognition',
  },
  dictationOnDesc: {
    id: 'app.captions.pad.dictationOnDesc',
    description: 'Aria description for button that turns on speech recognition',
  },
  dictationOffDesc: {
    id: 'app.captions.pad.dictationOffDesc',
    description: 'Aria description for button that turns off speech recognition',
  },
});

const propTypes = {
  locale: PropTypes.string.isRequired,
  ownerId: PropTypes.string.isRequired,
  padId: PropTypes.string.isRequired,
  readOnlyPadId: PropTypes.string.isRequired,
  name: PropTypes.string.isRequired,
  amIModerator: PropTypes.bool.isRequired,
  editCaptions: PropTypes.func.isRequired,
  initVoiceRecognition: PropTypes.func.isRequired,
  intl: PropTypes.shape({
    formatMessage: PropTypes.func.isRequired,
  }).isRequired,
};

class Pad extends Component {
  constructor(props) {
    super(props);

    this.state = {
      listening: false,
      text: '',
    };

    const { initVoiceRecognition } = props;
    this.recognition = initVoiceRecognition();

    this.toggleListen = this.toggleListen.bind(this);
    this.handleListen = this.handleListen.bind(this);
  }

  shouldComponentUpdate(nextProps, nextState) {
    const {
      text,
      listening,
    } = this.state;

    const padTextUpdate = nextState.text !== text && nextState.text !== '';
    const listeningUpdate = nextState.listening !== listening;

    if (padTextUpdate || listeningUpdate) {
      return true;
    }

    return false;
  }

  componentDidUpdate() {
    const {
      editCaptions,
    } = this.props;

    const {
      text,
    } = this.state;

    if (text !== '') {
      editCaptions(text);
    }
  }

  toggleListen() {
    const { listening, text } = this.state;

    this.setState({
      listening: !listening,
      text: !listening ? text : '',
    }, this.handleListen);
  }

  handleListen() {
    const {
      listening,
      text,
    } = this.state;

    if (listening) this.recognition.start();
    if (!listening) this.recognition.stop();

    let finalTranscript = '';
    this.recognition.onresult = (event) => {
      let interimTranscript = '';

      for (let i = event.resultIndex; i < event.results.length; i += 1) {
        const { transcript } = event.results[i][0];
        if (event.results[i].isFinal) finalTranscript += `${transcript} `;
        else interimTranscript += transcript;
      }

      if (this.itermResultContainer) {
        this.itermResultContainer.innerHTML = interimTranscript;
      }

      if (finalTranscript !== '' && finalTranscript !== text) {
        this.setState({ text: finalTranscript });
        finalTranscript = '';
      }
    };

    this.recognition.onerror = (event) => {
      console.log(`Error occurred in recognition: ${event.error}`);
    };
  }

  render() {
    const {
      locale,
      intl,
      padId,
      readOnlyPadId,
      ownerId,
      name,
      amIModerator,
    } = this.props;

    const { listening } = this.state;

    if (!amIModerator) {
      Session.set('openPanel', 'userlist');
      return null;
    }

    const url = PadService.getPadURL(padId, readOnlyPadId, ownerId);

    return (
      <div className={styles.pad}>
        <header className={styles.header}>
          <div className={styles.title}>
            <Button
              onClick={() => { Session.set('openPanel', 'userlist'); }}
              aria-label={intl.formatMessage(intlMessages.hide)}
              label={name}
              icon="left_arrow"
              className={styles.hideBtn}
            />
          </div>
          <span>
            <Button
              onClick={() => { this.toggleListen(); }}
              label={listening
                ? intl.formatMessage(intlMessages.dictationStop)
                : intl.formatMessage(intlMessages.dictationStart)
              }
              aria-describedby="dictationBtnDesc"
              color="primary"
            />
            <div id="dictationBtnDesc" hidden>
              {listening
                ? intl.formatMessage(intlMessages.dictationOffDesc)
                : intl.formatMessage(intlMessages.dictationOnDesc)
              }
            </div>
          </span>
          {CaptionsService.canIOwnThisPad(ownerId)
            ? (
              <Button
                icon="pen_tool"
                size="sm"
                ghost
                color="dark"
                hideLabel
                onClick={() => { CaptionsService.takeOwnership(locale); }}
                aria-label={intl.formatMessage(intlMessages.takeOwnership)}
                label={intl.formatMessage(intlMessages.takeOwnership)}
              />
            ) : null
        }
        </header>
        {listening ? (
          <div>
            <span className={styles.intermTitle}>Interm results</span>
            <div
              className={styles.processing}
              ref={(node) => { this.itermResultContainer = node; }}
            />
          </div>
        ) : null
      }
        <iframe
          title="etherpad"
          src={url}
          aria-describedby="padEscapeHint"
        />
        <span id="padEscapeHint" className={styles.hint} aria-hidden>
          {intl.formatMessage(intlMessages.tip)}
        </span>
      </div>
    );
  }
}

export default injectWbResizeEvent(injectIntl(Pad));

Pad.propTypes = propTypes;
