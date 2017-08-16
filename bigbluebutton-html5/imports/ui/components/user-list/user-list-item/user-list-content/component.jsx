import React, { Component } from 'react';
import { defineMessages } from 'react-intl';
import cx from 'classnames';
import { findDOMNode } from 'react-dom';
import UserAvatar from '/imports/ui/components/user-avatar/component';
import Icon from '/imports/ui/components/icon/component';
import Dropdown from '/imports/ui/components/dropdown/component';
import DropdownTrigger from '/imports/ui/components/dropdown/trigger/component';
import DropdownContent from '/imports/ui/components/dropdown/content/component';
import DropdownList from '/imports/ui/components/dropdown/list/component';
import DropdownListSeparator from '/imports/ui/components/dropdown/list/separator/component';
import DropdownListTitle from '/imports/ui/components/dropdown/list/title/component';
import styles from './../styles.scss';
import UserName from './../user-name/component';
import UserIcons from './../user-icons/component';

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

class UserListContent extends Component {
  constructor(props) {
    super(props);

    this.state = {
      isActionsOpen: false,
      dropdownOffset: 0,
      dropdownDirection: 'top',
      dropdownVisible: false,
    };

    this.handleScroll = this.handleScroll.bind(this);
    this.onActionsShow = this.onActionsShow.bind(this);
    this.onActionsHide = this.onActionsHide.bind(this);
    this.getDropdownMenuParent = this.getDropdownMenuParent.bind(this);
  }

  componentDidUpdate() {
    this.checkDropdownDirection();
  }

  onActionsShow() {
    const dropdown = findDOMNode(this.dropdown);
    const scrollContainer = dropdown.parentElement.parentElement;
    const dropdownTrigger = dropdown.children[0];

    this.setState({
      isActionsOpen: true,
      dropdownVisible: false,
      dropdownOffset: dropdownTrigger.offsetTop - scrollContainer.scrollTop,
      dropdownDirection: 'top',
    });

    scrollContainer.addEventListener('scroll', this.handleScroll, false);
  }

  onActionsHide() {
    this.setState({
      isActionsOpen: false,
      dropdownVisible: false,
    });

    findDOMNode(this).parentElement.removeEventListener('scroll', this.handleScroll, false);
  }

  getDropdownMenuParent() {
    return findDOMNode(this.dropdown);
  }

  handleScroll() {
    this.setState({
      isActionsOpen: false,
    });
  }


  /**
   * Check if the dropdown is visible, if so, check if should be draw on top or bottom direction.
   */
  checkDropdownDirection() {
    if (this.isDropdownActivedByUser()) {
      const dropdown = findDOMNode(this.dropdown);
      const dropdownTrigger = dropdown.children[0];
      const dropdownContent = dropdown.children[1];

      const scrollContainer = dropdown.parentElement.parentElement;

      const nextState = {
        dropdownVisible: true,
      };

      const isDropdownVisible =
        this.checkIfDropdownIsVisible(dropdownContent.offsetTop, dropdownContent.offsetHeight);

      if (!isDropdownVisible) {
        const offsetPageTop =
          ((dropdownTrigger.offsetTop + dropdownTrigger.offsetHeight) - scrollContainer.scrollTop);

        nextState.dropdownOffset = window.innerHeight - offsetPageTop;
        nextState.dropdownDirection = 'bottom';
      }

      this.setState(nextState);
    }
  }

  /**
   * Return true if the content fit on the screen, false otherwise.
   *
   * @param {number} contentOffSetTop
   * @param {number} contentOffsetHeight
   * @return True if the content fit on the screen, false otherwise.
   */
  checkIfDropdownIsVisible(contentOffSetTop, contentOffsetHeight) {
    return (contentOffSetTop + contentOffsetHeight) < window.innerHeight;
  }

  /**
  * Check if the dropdown is visible and is opened by the user
  *
  * @return True if is visible and opened by the user.
  */
  isDropdownActivedByUser() {
    const { isActionsOpen, dropdownVisible } = this.state;
    if (isActionsOpen && dropdownVisible) {
      this.focusDropdown();
    }
    return isActionsOpen && !dropdownVisible;
  }

  focusDropdown() {
    const list = findDOMNode(this.list);
    for (let i = 0; i < list.children.length; i++) {
      if (list.children[i].getAttribute('role') === 'menuitem') {
        list.children[i].focus();
        break;
      }
    }

    // The list children is a instance of HTMLCollection, there is no find, some, etc methods
    /* const childrens = [].slice.call(list.children);

    childrens.find(child => child.getAttribute('role') === 'menuitem').focus(); */
  }

  render() {
    const {
    compact,
      user,
      intl,
      normalizeEmojiName,
      actions,
  } = this.props;

    const {
    isActionsOpen,
      dropdownVisible,
      dropdownDirection,
      dropdownOffset,
  } = this.state;
    const userItemContentsStyle = {};

    userItemContentsStyle[styles.userItemContentsCompact] = compact;
    userItemContentsStyle[styles.active] = isActionsOpen;

    const you = (user.isCurrent) ? intl.formatMessage(messages.you) : '';

    const presenter = (user.isPresenter)
      ? intl.formatMessage(messages.presenter)
      : '';

    const userAriaLabel = intl.formatMessage(messages.userAriaLabel,
      {
        0: user.name,
        1: presenter,
        2: you,
        3: user.emoji.status,
      });

    const contents = (
      <div
        className={!actions.length ? cx(styles.userListItem, userItemContentsStyle) : null}
        aria-label={userAriaLabel}
      >
        <div className={styles.userItemContents} aria-hidden="true">
          <div className={styles.userAvatar}>
            <UserAvatar
              moderator={user.isModerator}
              presenter={user.isPresenter}
              talking={user.isTalking}
              muted={user.isMuted}
              listenOnly={user.isListenOnly}
              voice={user.isVoiceUser}
              color={user.color}
            >
              {user.emoji.status !== 'none' ?
                <Icon iconName={normalizeEmojiName(user.emoji.status)} /> :
                user.name.toLowerCase().slice(0, 2)}
            </UserAvatar>
          </div>
          {<UserName
            user={user}
            compact={compact}
            intl={intl}
          />}
          {<UserIcons
            user={user}
            compact={compact}
          />}
        </div>
      </div>
    );

    if (!actions.length) {
      return contents;
    }

    return (
      <Dropdown
        ref={(ref) => { this.dropdown = ref; }}
        isOpen={this.state.isActionsOpen}
        onShow={this.onActionsShow}
        onHide={this.onActionsHide}
        className={cx(styles.dropdown, styles.userListItem, userItemContentsStyle)}
        autoFocus={false}
        aria-haspopup="true"
        aria-live="assertive"
        aria-relevant="additions"
      >
        <DropdownTrigger>
          {contents}
        </DropdownTrigger>
        <DropdownContent
          style={{
            visibility: dropdownVisible ? 'visible' : 'hidden',
            [dropdownDirection]: `${dropdownOffset}px`,
          }}
          className={styles.dropdownContent}
          placement={`right ${dropdownDirection}`}
        >

          <DropdownList
            ref={(ref) => { this.list = ref; }}
            getDropdownMenuParent={this.getDropdownMenuParent}
            onActionsHide={this.onActionsHide}
          >
            {
              [
                (<DropdownListTitle
                  description={intl.formatMessage(messages.menuTitleContext)}
                  key={_.uniqueId('dropdown-list-title')}
                >
                  {user.name}
                </DropdownListTitle>),
                (<DropdownListSeparator key={_.uniqueId('action-separator')} />),
              ].concat(actions)
            }
          </DropdownList>
        </DropdownContent>
      </Dropdown>
    );
  }
}

export default UserListContent;
