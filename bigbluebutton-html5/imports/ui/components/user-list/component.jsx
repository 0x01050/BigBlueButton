import React, { Component, PropTypes } from 'react';
import { withRouter } from 'react-router';
import ReactCSSTransitionGroup from 'react-addons-css-transition-group';
import styles from './styles.scss';
import cx from 'classnames';
import { defineMessages, injectIntl } from 'react-intl';
import UserListItem from './user-list-item/component.jsx';
import ChatListItem from './chat-list-item/component.jsx';
import { findDOMNode } from 'react-dom';

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
      compact: this.props.compact,
    };

    this.rovingIndex = this.rovingIndex.bind(this);
    this.j = 0;
    this.msg = false;
    this.use = false;
  }

  componentDidUpdate() {
  }

  rovingIndex(event) {
    const {users} = this.props;

    let list = findDOMNode(this.refs.usersList);
    let items = findDOMNode(this.refs.users);

    if (event.keyCode === 13) {
      let active = document.activeElement;
      active.firstChild.click();
    }

    if (event.keyCode === 40) {
      let active = document.activeElement;
      if (this.j <= users.length - 1) {
        console.log("J value : " + this.j);
        active.tabIndex = -1;
        items.childNodes[this.j].tabIndex = 0;
        let newFocus = items.childNodes[this.j];
        this.j++;
        console.log(this.j);
        newFocus.focus();
      }else{
        this.j = 0;
        active.tabIndex = -1;
        list.tabIndex = 0;
        list.focus();
      }
    }

    if (event.keyCode === 38) {
      let active = document.activeElement;
      if (this.j > 0) {
        this.j--;
        active.tabIndex = -1;
        items.childNodes[this.j].tabIndex = 0;
        let newFocus = items.childNodes[this.j];
        newFocus.focus();
      }else if (this.j <= 0) {
        this.j = users.length;
        active.tabIndex = -1;
        list.tabIndex = 0;
        list.focus();
      }
    }

  }

  componentDidMount() {

    if (!this.state.compact) {
      let messageList = findDOMNode(this.refs.messages);
      let messageItems = findDOMNode(this.refs.msgs);
      let ItemsCount = messageItems.childElementCount;
      let i = ItemsCount;
      let testing = "HI";

      this.refs.messagesList.addEventListener("keypress", this.rovingIndex.call(this, testing));

      this.refs.usersList.addEventListener("keypress", this.rovingIndex);

      //////////////////////////////////////////
      /////////////////////////////////////////
/*
      userList.addEventListener("keypress", function(e){

        if (e.keyCode === 13) {
          let active = document.activeElement;
          active.firstChild.click();
        }

        if (e.keyCode === 40) {
          let active = document.activeElement;
          if (j <= usersCount - 1) {
            active.tabIndex = -1;
            userItems.childNodes[j].tabIndex = 0;
            let newFocus = userItems.childNodes[j];
            newFocus.focus();
            j++;
          }else{
            j = 0;
            active.tabIndex = -1;
            userList.tabIndex = 0;
            userList.focus();
          }
        }

        if (e.keyCode === 38) {
          let active = document.activeElement;

          if (j > 0) {
            j--;
            active.tabIndex = -1;
            userItems.childNodes[j].tabIndex = 0;
            let newFocus = userItems.childNodes[j];
            newFocus.focus();
          }else if (j <= 0) {
            j = usersCount;
            active.tabIndex = -1;
            userList.tabIndex = 0;
            userList.focus();
          }
        }

      });

      /////////////////////////////////////////
      /////////////////////////////////////////

      messageList.addEventListener("keypress", function(e){
        if (e.keyCode === 40) {
          let active = document.activeElement;
          if (i <= ItemsCount - 1) {
            active.tabIndex = -1;
            messageItems.childNodes[i].tabIndex = 0;
            let newFocus = messageItems.childNodes[i];
            newFocus.focus();
            i++;
          }else{
            i = 0;
            active.tabIndex = -1;
            messageList.tabIndex = 0;
            messageList.focus();
          }
        }

        if (e.keyCode === 38) {
          let active = document.activeElement;

          if (i > 0) {
            i--;
            active.tabIndex = -1;
            messageItems.childNodes[i].tabIndex = 0;
            let newFocus = messageItems.childNodes[i];
            newFocus.focus();
          }else if (i <= 0) {
            i = ItemsCount;
            active.tabIndex = -1;
            messageList.tabIndex = 0;
            messageList.focus();
          }
        }

      }); */
    }
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
    const { intl } = this.props;

    return (
      <div className={styles.header}>
        {
          !this.state.compact ?
          <h2 className={styles.headerTitle}>
            {intl.formatMessage(intlMessages.participantsTitle)}
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

  renderMessages() {
    const {
      openChats,
      openChat,
      intl,
    } = this.props;

    return (
      <div className={styles.messages}>
        {
          !this.state.compact ?
          <h3 className={styles.smallTitle}>
            {intl.formatMessage(intlMessages.messagesTitle)}
          </h3> : <hr className={styles.separator}></hr>
        }
        <div className={styles.scrollableList} tabIndex={0} ref="messagesList" id="messageWrap">
          <ReactCSSTransitionGroup
            transitionName={listTransition}
            transitionAppear={true}
            transitionEnter={true}
            transitionLeave={false}
            transitionAppearTimeout={0}
            transitionEnterTimeout={0}
            transitionLeaveTimeout={0}
            component="ul"
            className={cx(styles.chatsList, styles.scrollableList)} ref="msgs">
              {openChats.map(chat => (
                <ChatListItem
                  compact={this.state.compact}
                  key={chat.id}
                  openChat={openChat}
                  chat={chat}
                  tabIndex={-1} />
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
      isBreakoutRoom,
      intl,
      callServer,
    } = this.props;

    const userActions = {
      openChat: {
        label: intl.formatMessage(intlMessages.ChatLabel),
        handler: (router, user) => router.push(`/users/chat/${user.id}`),
        icon: 'chat',
      },
      clearStatus: {
        label: intl.formatMessage(intlMessages.ClearStatusLabel),
        handler: user => callServer('setEmojiStatus', user.id, 'none'),
        icon: 'clear_status',
      },
      setPresenter: {
        label: intl.formatMessage(intlMessages.MakePresenterLabel),
        handler: user => callServer('assignPresenter', user.id),
        icon: 'presentation',
      },
      kick: {
        label: intl.formatMessage(intlMessages.KickUserLabel),
        handler: user => callServer('kickUser', user.id),
        icon: 'circle_close',
      },
      mute: {
        label: intl.formatMessage(intlMessages.MuteUserAudioLabel),
        handler: user => callServer('muteUser', user.id),
        icon: 'audio_off',
      },
      unmute: {
        label: intl.formatMessage(intlMessages.UnmuteUserAudioLabel),
        handler: user => callServer('unmuteUser', user.id),
        icon: 'audio_on',
      },
    };

    return (
      <div className={styles.participants}>
        {
          !this.state.compact ?
          <h3 className={styles.smallTitle}>
            {intl.formatMessage(intlMessages.usersTitle)}
            &nbsp;({users.length})
          </h3> : <hr className={styles.separator}></hr>
        }
        <div className={styles.scrollableList} tabIndex={0} ref="usersList" id="usersWrap">
        <ReactCSSTransitionGroup
          transitionName={listTransition}
          transitionAppear={true}
          transitionEnter={true}
          transitionLeave={true}
          transitionAppearTimeout={0}
          transitionEnterTimeout={0}
          transitionLeaveTimeout={0}
          component="ul"
          className={cx(styles.participantsList, styles.scrollableList)} ref="users">
          {
            users.map(user => (
            <UserListItem
              compact={this.state.compact}
              key={user.id}
              isBreakoutRoom={isBreakoutRoom}
              user={user}
              currentUser={currentUser}
              userActions={userActions}
            />
          ))}
        </ReactCSSTransitionGroup>
        </div>
      </div>
    );
  }
}

const intlMessages = defineMessages({
  usersTitle: {
    id: 'app.userlist.usersTitle',
    description: 'Title for the Header',
  },
  messagesTitle: {
    id: 'app.userlist.messagesTitle',
    description: 'Title for the messages list',
  },
  participantsTitle: {
    id: 'app.userlist.participantsTitle',
    description: 'Title for the Users list',
  },
  ChatLabel: {
    id: 'app.userlist.menu.chat.label',
    description: 'Save the changes and close the settings menu',
  },
  ClearStatusLabel: {
    id: 'app.userlist.menu.clearStatus.label',
    description: 'Clear the emoji status of this user',
  },
  MakePresenterLabel: {
    id: 'app.userlist.menu.makePresenter.label',
    description: 'Set this user to be the presenter in this meeting',
  },
  KickUserLabel: {
    id: 'app.userlist.menu.kickUser.label',
    description: 'Forcefully remove this user from the meeting',
  },
  MuteUserAudioLabel: {
    id: 'app.userlist.menu.muteUserAudio.label',
    description: 'Forcefully mute this user',
  },
  UnmuteUserAudioLabel: {
    id: 'app.userlist.menu.unmuteUserAudio.label',
    description: 'Forcefully unmute this user',
  },
});

UserList.propTypes = propTypes;
export default withRouter(injectIntl(UserList));
