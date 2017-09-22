import React from 'react';
import PropTypes from 'prop-types';
import { defineMessages } from 'react-intl';
import Icon from '/imports/ui/components/icon/component';
import styles from './styles.scss';


const messages = defineMessages({
  presenter: {
    id: 'app.userlist.presenter',
    description: 'Text for identifying presenter user',
  },
  you: {
    id: 'app.userlist.you',
    description: 'Text for identifying your user',
  },
  locked: {
    id: 'app.userlist.locked',
    description: 'Text for identifying locked user',
  },
  guest: {
    id: 'app.userlist.guest',
    description: 'Text for identifying guest user',
  },
  menuTitleContext: {
    id: 'app.userlist.menuTitleContext',
    description: 'adds context to userListItem menu title',
  },
  userAriaLabel: {
    id: 'app.userlist.userAriaLabel',
    description: 'aria label for each user in the userlist',
  },
});
const propTypes = {
  user: PropTypes.shape({
    name: PropTypes.string.isRequired,
    isPresenter: PropTypes.bool.isRequired,
    isVoiceUser: PropTypes.bool.isRequired,
    isModerator: PropTypes.bool.isRequired,
    image: PropTypes.string,
  }).isRequired,
  compact: PropTypes.bool.isRequired,
  intl: PropTypes.shape({}).isRequired,
  meeting: PropTypes.shape({}).isRequired,
  isMeetingLocked: PropTypes.func.isRequired,
};

const UserName = (props) => {
  const {
    user,
    intl,
    compact,
    isMeetingLocked,
    meeting,
  } = props;

  if (compact) {
    return null;
  }

  const userNameSub = [];

  if (compact) {
    return null;
  }

  const isViewer = !(user.isPresenter || user.isModerator);

  if (isMeetingLocked(meeting.meetingId) && isViewer && user.isLocked) {
    userNameSub.push(<span>
      <Icon iconName="lock" />
      {intl.formatMessage(messages.locked)}
    </span>);
  }

  if (user.isGuest) {
    userNameSub.push(intl.formatMessage(messages.guest));
  }


  return (
    <div className={styles.userName}>
      <span className={styles.userNameMain}>
        {user.name} <i>{(user.isCurrent) ? `(${intl.formatMessage(messages.you)})` : ''}</i>
      </span>
      {
        userNameSub.length ?
          <span className={styles.userNameSub}>
            {userNameSub.reduce((prev, curr) => [prev, ' | ', curr])}
          </span>
        : null
      }
    </div>
  );
};

UserName.propTypes = propTypes;
export default UserName;
