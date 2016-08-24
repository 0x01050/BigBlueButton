import React, { Component, PropTyes } from 'react';
import ReactDOM from 'react-dom';
import Icon from '/imports/ui/components/icon/component';
import Button from '/imports/ui/components/button/component';
import classNames from 'classnames';
import styles from './styles';
import { FormattedMessage } from 'react-intl';
import SettingsModal from '../settings/SettingsModal';
import SessionMenu from '../settings/submenus/SessionMenu';
import Dropdown from './Dropdown';
import DropdownContent from './DropdownContent';
import DropdownTrigger from './DropdownTrigger';

export default class SettingsDropdown extends Component {
  constructor(props) {
    super(props);
    this.menus = [];
    this.openWithKey = this.openWithKey.bind(this);
  }

  componentWillMount() {
    this.setState({ activeMenu: -1, focusedMenu: 0, });
    this.menus.push({ className: '',
        props: { title: 'Fullscreen', prependIconName: 'icon-', icon: 'bbb-full-screen', },
        tabIndex: 1, });
    this.menus.push({ className: SettingsModal,
        props: { title: 'Settings', prependIconName: 'icon-', icon: 'bbb-more', },
        tabIndex: 2, });
    this.menus.push({ className: SessionMenu,
        props: { title: 'Leave Session', prependIconName: 'icon-', icon: 'bbb-logout', },
        tabIndex: 3, });
  }

  componentWillUpdate() {
    const DROPDOWN = this.refs.dropdown;
    if (DROPDOWN.state.isMenuOpen && this.state.activeMenu >= 0) {
      this.setState({ activeMenu: -1, focusedMenu: 0, });
    }
  }

  setFocus() {
    ReactDOM.findDOMNode(this.refs[`menu${this.state.focusedMenu}`]).focus();
  }

  handleListKeyDown(event) {
    const pressedKey = event.keyCode;
    let numOfMenus = this.menus.length - 1;

    // User pressed tab
    if (pressedKey === 9) {
      let newIndex = 0;
      if (this.state.focusedMenu >= numOfMenus) { // Checks if at end of menu
        newIndex = 0;
        if (!event.shiftKey) {
          this.refs.dropdown.hideMenu();
        }
      } else {
        newIndex = this.state.focusedMenu;
      }

      this.setState({ focusedMenu: newIndex, });
      return;
    }

    // User pressed shift + tab
    if (event.shiftKey && pressedKey === 9) {
      let newIndex = 0;
      if (this.state.focusedMenu <= 0) { // Checks if at beginning of menu
        newIndex = numOfMenus;
      } else {
        newIndex = this.state.focusedMenu - 1;
      }

      this.setState({ focusedMenu: newIndex, });
      return;
    }

    // User pressed up key
    if (pressedKey === 38) {
      if (this.state.focusedMenu <= 0) { // Checks if at beginning of menu
        this.setState({ focusedMenu: numOfMenus, },
           () => { this.setFocus(); });
      } else {
        this.setState({ focusedMenu: this.state.focusedMenu - 1, },
           () => { this.setFocus(); });
      }
      return;
    }

    // User pressed down key
    if (pressedKey === 40) {
      if (this.state.focusedMenu >= numOfMenus) { // Checks if at end of menu
        this.setState({ focusedMenu: 0, },
           () => { this.setFocus(); });
      } else {
        this.setState({ focusedMenu: this.state.focusedMenu + 1, },
           () => { this.setFocus(); });
      }
      return;
    }

    // User pressed enter and spaceBar
    if (pressedKey === 13 || pressedKey === 32) {
      this.clickMenu(this.state.focusedMenu);
      return;
    }

    //User pressed ESC
    if (pressedKey == 27) {
      this.setState({ activeMenu: -1, focusedMenu: 0, });
      this.refs.dropdown.hideMenu();
    }
    return;
  }

  handleFocus(index) {
    this.setState({ focusedMenu: index, },
       () => { this.setFocus(); });
  }

  clickMenu(i) {
    this.setState({ activeMenu: i, });
    this.refs.dropdown.hideMenu();
  }

  createMenu() {
    const curr = this.state.activeMenu;

    switch (curr) {
      case 0:
        console.log(this.menus[curr].props.title);
        break;
      case 1:
        return <SettingsModal />;
        break;
      case 2:
        return <SessionMenu />;
        break;
      default:
        return;
    }
  }

  openWithKey(event) {
    // Focus first menu option
    if (event.keyCode === 9) {
      event.preventDefault();
      event.stopPropagation();
    }

    this.setState({ focusedMenu: 0 }, () => { this.setFocus(); });
  }

  renderAriaLabelsDescs(i) {
    switch (i) {
      case 0:
        return (
          <p id="fullScreen" hidden>
          <FormattedMessage
            id="app.modals.dropdown.fullScreen"
            description="Aria label for fullscreen"
            defaultMessage="Make fullscreen"
          />
        </p>
        );
        break;
      case 1:
        return (
        <p id="settingsModal" hidden>
          <FormattedMessage
            id="app.modals.dropdown.settingsModal"
            description="Aria label for settings"
            defaultMessage="Open Settings"
          />
        </p>
        );
        break;
      case 2:
        return (
        <p id="leaveSession" hidden>
          <FormattedMessage
            id="app.modals.dropdown.leaveSession"
            description="Aria label for logout"
            defaultMessage="Logout"
          />
        </p>
        );
        break;
      default:
        return;
    }

  }

  render() {

    return (
      <div>
        <Dropdown ref='dropdown' focusMenu={this.openWithKey}>
          <DropdownTrigger labelBtn='setting' iconBtn='more' />
          <DropdownContent>
            <div className={styles.triangleOnDropdown}></div>
            <div className={styles.dropdownActiveContent}>
              <ul className={styles.menuList} role="menu">
                {this.menus.map((value, index) => (
                  <li
                    key={index}
                    role='menuitem'
                    tabIndex={value.tabIndex}
                    onClick={this.clickMenu.bind(this, index)}
                    onKeyDown={this.handleListKeyDown.bind(this)}
                    onFocus={this.handleFocus.bind(this, index)}
                    ref={'menu' + index}
                    className={styles.settingsMenuItem}>

                    <Icon
                      key={index}
                      prependIconName={value.props.prependIconName}
                      iconName={value.props.icon}
                      title={value.props.title}
                      className={styles.iconColor}/>

                    <span className={styles.settingsMenuItemText}>{value.props.title}</span>
                    {index == '0' ? <hr className={styles.hrDropdown}/> : null}
                    {this.renderAriaLabelsDescs(index)}
                  </li>
                ))}
              </ul>
            </div>
          </DropdownContent>
        </Dropdown>
        <div role='presentation'>{this.createMenu()}</div>
        <p id="settingsDropdown" hidden>
          <FormattedMessage
            id="app.modals.dropdown.settingsDropdown"
            description="Aria label for Options"
            defaultMessage="Options"
          />
        </p>
      </div>
    );
  }
}
