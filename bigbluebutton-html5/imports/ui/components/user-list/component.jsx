import React, { Component, PropTypes } from 'react';
import { withRouter } from 'react-router';
import ReactCSSTransitionGroup from 'react-addons-css-transition-group';
import styles from './styles.scss';
import { FormattedMessage } from 'react-intl';
import cx from 'classnames';

import UserListItem from './user-list-item/component.jsx';
import ChatListItem from './chat-list-item/component.jsx';

const propTypes = {
  openChats: PropTypes.array.isRequired,
  users: PropTypes.array.isRequired,
};

const defaultProps = {
};

const listTransition = {
  enter: styles.enter,
  enterActive: styles.enterActive,
  appear: styles.appear,
  appearActive: styles.appearActive,
  leave: styles.leave,
  leaveActive: styles.leaveActive,
};

class UserList extends Component {
  constructor(props) {
    super(props);

    this.state = {
      compact: false,
      participantsScrollable: true,
    };
  }

  render() {
    return (
      <div className={styles.userList}>
        {this.renderHeader()}
        {this.renderContent()}
      </div>
    );
  }

  renderHeader() {
    return (
      <div className={styles.header}>
        {!this.state.compact ?
          <h2 className={styles.headerTitle}>
            <FormattedMessage
              id="app.userlist.participantsTitle"
              description="Title for the Header"
              defaultMessage="Participants"
            />
          </h2> : null
        }
      </div>
    );
  }

  renderContent() {
    return (
      <div className={styles.content}>
        {this.renderMessages()}
        {this.renderParticipants()}
      </div>
    );
  }

  openActions() {
    console.log('4HEAD');
    this.setState({
      participantsScrollable: false,
    });
  }

  closeActions() {
    this.setState({
      participantsScrollable: true,
    });
  }

  renderMessages() {
    const {
      openChats,
      openChat,
    } = this.props;

    return (
      <div className={styles.messages}>
        {!this.state.compact ?
          <h3 className={styles.smallTitle}>
            <FormattedMessage
              id="app.userlist.messagesTitle"
              description="Title for the messages list"
              defaultMessage="Messages"
            />
          </h3> : <hr className={styles.separator}></hr>
        }
        <div className={styles.scrollableList}>
          <ReactCSSTransitionGroup
            transitionName={listTransition}
            transitionAppear={true}
            transitionEnter={true}
            transitionLeave={false}
            transitionAppearTimeout={0}
            transitionEnterTimeout={0}
            transitionLeaveTimeout={0}
            component="ul"
            className={cx(styles.chatsList, styles.scrollableList)}>
              {openChats.map(chat => (
                <ChatListItem
                  compact={this.state.compact}
                  key={chat.id}
                  openChat={openChat}
                  chat={chat} />
              ))}
          </ReactCSSTransitionGroup>
        </div>
      </div>
    );
  }

  renderParticipants() {
    const {
      users,
      currentUser,
      userActions,
    } = this.props;

    return (
      <div className={styles.participants}>
        {!this.state.compact ?
          <h3 className={styles.smallTitle}>
            <FormattedMessage
              id="app.userlist.participantsTitle"
              description="Title for the Participants list"
              defaultMessage="Participants"
            />
            &nbsp;({this.props.users.length})
          </h3> : <hr className={styles.separator}></hr>
        }
        <ReactCSSTransitionGroup
          transitionName={listTransition}
          transitionAppear={true}
          transitionEnter={true}
          transitionLeave={true}
          transitionAppearTimeout={0}
          transitionEnterTimeout={0}
          transitionLeaveTimeout={0}
          component="ul"
          className={cx(styles.participantsList, styles.scrollableList)}>
          {users.map(user => (
            <UserListItem
              compact={this.state.compact}
              key={user.id}
              user={user}
              currentUser={currentUser}
              userActions={userActions}
              onUserActionsOpen={() => this.openActions()}
              onUserActionsClose={() => this.closeActions()}
            />
          ))}
        </ReactCSSTransitionGroup>
      </div>
    );
  }
}

UserList.propTypes = propTypes;
export default withRouter(UserList);
