import React, { Component } from 'react';
import PropTypes from 'prop-types';
import cx from 'classnames';
import { defineMessages } from 'react-intl';
import Icon from '/imports/ui/components/icon/component';
import NoteService from '/imports/ui/components/note/service';
import { styles } from './styles';

const propTypes = {
  intl: PropTypes.shape({
    formatMessage: PropTypes.func.isRequired,
  }).isRequired,
  revs: PropTypes.number.isRequired,
  isPanelOpened: PropTypes.bool.isRequired,
};

const intlMessages = defineMessages({
  notesTitle: {
    id: 'app.userList.notesTitle',
    description: 'Title for the notes list',
  },
  title: {
    id: 'app.note.title',
    description: 'Title for the shared notes',
  },
});

class UserNotes extends Component {
  static getDerivedStateFromProps(props, state) {
    const { isPanelOpened, revs } = props;
    const { unread, revs: revsState } = state;

    if (!isPanelOpened && !unread) {
      if (revsState !== revs) return ({ unread: true });
    }

    if (isPanelOpened && unread) {
      return ({ unread: false });
    }

    return null;
  }

  constructor(props) {
    super(props);

    this.state = {
      unread: false,
    };
  }

  componentDidMount() {
    const { revs } = this.props;

    const stateValues = {
      revs,
      unread: revs !== 0,
    };

    this.setState(stateValues);
  }

  render() {
    const { intl, isPanelOpened } = this.props;
    const { unread } = this.state;

    if (!NoteService.isEnabled()) return null;

    const toggleNotePanel = () => {
      Session.set(
        'openPanel',
        isPanelOpened
          ? 'userlist'
          : 'note',
      );
    };

    const iconClasses = {};
    iconClasses[styles.notification] = unread;

    const linkClasses = {};
    linkClasses[styles.active] = isPanelOpened;

    return (
      <div className={styles.messages}>
        {
          <h2 className={styles.smallTitle}>
            {intl.formatMessage(intlMessages.notesTitle)}
          </h2>
        }
        <div className={styles.scrollableList}>
          <div
            role="button"
            tabIndex={0}
            className={cx(styles.noteLink, linkClasses)}
            onClick={toggleNotePanel}
          >
            <Icon iconName="copy" className={cx(iconClasses)} />
            <span>{intl.formatMessage(intlMessages.title)}</span>
          </div>
        </div>
      </div>
    );
  }
}

UserNotes.propTypes = propTypes;

export default UserNotes;
