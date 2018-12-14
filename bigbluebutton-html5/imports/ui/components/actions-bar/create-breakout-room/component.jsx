import React, { Component } from 'react';
import { defineMessages, injectIntl } from 'react-intl';
import _ from 'lodash';
import cx from 'classnames';
<<<<<<< HEAD
import browser from 'browser-detect';
import Button from '/imports/ui/components/button/component';
=======
import { Session } from 'meteor/session';
import Modal from '/imports/ui/components/modal/fullscreen/component';
import { withModalMounter } from '/imports/ui/components/modal/service';
>>>>>>> upstream/master
import HoldButton from '/imports/ui/components/presentation/presentation-toolbar/zoom-tool/holdButton/component';
import SortList from './sort-user-list/component';
import { styles } from './styles';
import Icon from '../../icon/component';

const intlMessages = defineMessages({
  breakoutRoomTitle: {
    id: 'app.createBreakoutRoom.title',
    description: 'modal title',
  },
  breakoutRoomDesc: {
    id: 'app.createBreakoutRoom.modalDesc',
    description: 'modal description',
  },
  confirmButton: {
    id: 'app.createBreakoutRoom.confirm',
    description: 'confirm button label',
  },
  dismissLabel: {
    id: 'app.presentationUploder.dismissLabel',
    description: 'used in the button that close modal',
  },
  numberOfRooms: {
    id: 'app.createBreakoutRoom.numberOfRooms',
    description: 'number of rooms label',
  },
  duration: {
    id: 'app.createBreakoutRoom.durationInMinutes',
    description: 'duration time label',
  },
  randomlyAssign: {
    id: 'app.createBreakoutRoom.randomlyAssign',
    description: 'randomly assign label',
  },
  roomName: {
    id: 'app.createBreakoutRoom.roomName',
    description: 'room intl to name the breakout meetings',
  },
  freeJoinLabel: {
    id: 'app.createBreakoutRoom.freeJoin',
    description: 'free join label',
  },
  roomLabel: {
    id: 'app.createBreakoutRoom.room',
    description: 'Room label',
  },
  leastOneWarnBreakout: {
    id: 'app.createBreakoutRoom.leastOneWarnBreakout',
    description: 'warn message label',
  },
  notAssigned: {
    id: 'app.createBreakoutRoom.notAssigned',
    description: 'Not assigned label',
  },
  breakoutRoomLabel: {
    id: 'app.createBreakoutRoom.breakoutRoomLabel',
    description: 'breakout room label',
  },
  addParticipantLabel: {
    id: 'app.createBreakoutRoom.addParticipantLabel',
    description: 'add Participant label',
  },
  nextLabel: {
    id: 'app.createBreakoutRoom.nextLabel',
    description: 'Next label',
  },
  backLabel: {
    id: 'app.audio.backLabel',
    description: 'Back label',
  },
});
const MIN_BREAKOUT_ROOMS = 2;
const MAX_BREAKOUT_ROOMS = 8;

class BreakoutRoom extends Component {
  constructor(props) {
    super(props);
    this.changeNumberOfRooms = this.changeNumberOfRooms.bind(this);
    this.changeDurationTime = this.changeDurationTime.bind(this);
    this.changeUserRoom = this.changeUserRoom.bind(this);
    this.increaseDurationTime = this.increaseDurationTime.bind(this);
    this.decreaseDurationTime = this.decreaseDurationTime.bind(this);
    this.onCreateBreakouts = this.onCreateBreakouts.bind(this);
    this.setRoomUsers = this.setRoomUsers.bind(this);
    this.setFreeJoin = this.setFreeJoin.bind(this);
    this.getUserByRoom = this.getUserByRoom.bind(this);
    this.renderUserItemByRoom = this.renderUserItemByRoom.bind(this);
    this.renderRoomsGrid = this.renderRoomsGrid.bind(this);
    this.renderBreakoutForm = this.renderBreakoutForm.bind(this);
    this.renderFreeJoinCheck = this.renderFreeJoinCheck.bind(this);
<<<<<<< HEAD
    this.renderRoomSortList = this.renderRoomSortList.bind(this);
    this.renderDesktop = this.renderDesktop.bind(this);
    this.renderMobile = this.renderMobile.bind(this);
    this.renderButtonSetLevel = this.renderButtonSetLevel.bind(this);
    this.renderSelectUserScreen = this.renderSelectUserScreen.bind(this);
=======
    this.handleDismiss = this.handleDismiss.bind(this);
>>>>>>> upstream/master

    this.state = {
      numberOfRooms: MIN_BREAKOUT_ROOMS,
      seletedId: '',
      users: [],
      durationTime: 1,
      freeJoin: false,
<<<<<<< HEAD
      formFillLevel: 1,
      roomSelected: 0,
=======
      preventClosing: true,
      valid: true,
>>>>>>> upstream/master
    };
  }

  componentDidMount() {
    this.setRoomUsers();
  }

  componentDidUpdate(prevProps, prevstate) {
    const { numberOfRooms } = this.state;
    if (numberOfRooms < prevstate.numberOfRooms) {
      this.resetUserWhenRoomsChange(numberOfRooms);
    }
  }

  onCreateBreakouts() {
    const {
      createBreakoutRoom,
      meetingName,
      intl,
    } = this.props;

    if (this.state.users.length === this.getUserByRoom(0).length) {
      this.setState({ valid: false });
      return;
    }
    this.setState({ preventClosing: false });
    const { numberOfRooms, durationTime } = this.state;
    const rooms = _.range(1, numberOfRooms + 1).map(value => ({
      users: this.getUserByRoom(value).map(u => u.userId),
      name: intl.formatMessage(intlMessages.roomName, {
        0: meetingName,
        1: value,
      }),
      freeJoin: this.state.freeJoin,
      sequence: value,
    }));

    createBreakoutRoom(rooms, durationTime, this.state.freeJoin);
    Session.set('isUserListOpen', true);
  }

  setRoomUsers() {
    const { users } = this.props;
    const roomUsers = users.map(user => ({
      userId: user.userId,
      userName: user.name,
      room: 0,
    }));

    this.setState({
      users: roomUsers,
    });
  }

  setFreeJoin(e) {
    this.setState({ freeJoin: e.target.checked });
  }

  getUserByRoom(room) {
    return this.state.users.filter(user => user.room === room);
  }

  handleDismiss() {
    const { mountModal } = this.props;

    return new Promise((resolve) => {
      mountModal(null);

      this.setState({
        preventClosing: false,
      }, resolve);
    });
  }

  resetUserWhenRoomsChange(rooms) {
    const { users } = this.state;
    const filtredUsers = users.filter(u => u.room > rooms);
    filtredUsers.forEach(u => this.changeUserRoom(u.userId, 0));
  }

  changeUserRoom(userId, room) {
    const { users } = this.state;
    const idxUser = users.findIndex(user => user.userId === userId);
    users[idxUser].room = room;
    this.setState({ users });
  }

  increaseDurationTime() {
    this.setState({ durationTime: (1 * this.state.durationTime) + 1 });
  }

  decreaseDurationTime() {
    const number = ((1 * this.state.durationTime) - 1);
    this.setState({ durationTime: number < 1 ? 1 : number });
  }

  changeDurationTime(event) {
    this.setState({ durationTime: Number.parseInt(event.target.value, 10) || '' });
  }

  changeNumberOfRooms(event) {
    this.setState({ numberOfRooms: Number.parseInt(event.target.value, 10) });
  }

  renderRoomsGrid() {
    const { intl } = this.props;

    const allowDrop = (ev) => {
      ev.preventDefault();
    };

    const drop = room => (ev) => {
      ev.preventDefault();
      const data = ev.dataTransfer.getData('text');
      this.changeUserRoom(data, room);
      this.setState({ seletedId: '' });
    };

    return (
      <div className={styles.boxContainer}>
        <label htmlFor="BreakoutRoom" className={!this.state.valid ? styles.changeToWarn : null}>
          <p
            className={styles.freeJoinLabel}
          >
            {intl.formatMessage(intlMessages.notAssigned, { 0: this.getUserByRoom(0).length })}
          </p>
          <div className={styles.breakoutBox} onDrop={drop(0)} onDragOver={allowDrop} >
            {this.renderUserItemByRoom(0)}
          </div>
          <span className={this.state.valid ? styles.dontShow : styles.leastOneWarn} >
            {intl.formatMessage(intlMessages.leastOneWarnBreakout)}
          </span>
        </label>
        {
          _.range(1, this.state.numberOfRooms + 1).map(value =>
            (
              <label htmlFor="BreakoutRoom" key={`room-${value}`}>
                <p
                  className={styles.freeJoinLabel}
                >
                  {intl.formatMessage(intlMessages.roomLabel, { 0: (value) })}
                </p>
                <div className={styles.breakoutBox} onDrop={drop(value)} onDragOver={allowDrop}>
                  {this.renderUserItemByRoom(value)}
                </div>
              </label>))
        }
      </div>
    );
  }

  renderBreakoutForm() {
    const { intl } = this.props;

    return (
      <div className={styles.breakoutSettings}>
        <label htmlFor="numberOfRooms">
          <p className={styles.labelText}>{intl.formatMessage(intlMessages.numberOfRooms)}</p>
          <select
            name="numberOfRooms"
            className={styles.inputRooms}
            value={this.state.numberOfRooms}
            onChange={this.changeNumberOfRooms}
          >
            {
              _.range(MIN_BREAKOUT_ROOMS, MAX_BREAKOUT_ROOMS + 1).map(item => (<option key={_.uniqueId('value-')}>{item}</option>))
            }
          </select>
        </label>
        <label htmlFor="breakoutRoomTime" >
          <p className={styles.labelText}>{intl.formatMessage(intlMessages.duration)}</p>
          <div className={styles.durationArea}>
            <input
              type="number"
              className={styles.duration}
              min={MIN_BREAKOUT_ROOMS}
              value={this.state.durationTime}
              onChange={this.changeDurationTime}
            />
            <span>
              <HoldButton
                key="decrease-breakout-time"
                exec={this.decreaseDurationTime}
                minBound={MIN_BREAKOUT_ROOMS}
                value={this.state.durationTime}
              >
                <Icon
                  className={styles.iconsColor}
                  iconName="substract"
                />
              </HoldButton>
              <HoldButton
                key="increase-breakout-time"
                exec={this.increaseDurationTime}
              >
                <Icon
                  className={styles.iconsColor}
                  iconName="add"
                />
              </HoldButton>

            </span>
          </div>
        </label>
        <p className={styles.randomText}>{intl.formatMessage(intlMessages.randomlyAssign)}</p>
      </div>
    );
  }

  renderSelectUserScreen() {
    return (
      <SortList
        confirm={() => this.setState({ formFillLevel: 2 })}
        users={this.state.users}
        room={this.state.roomSelected}
        onCheck={this.changeUserRoom}
        onUncheck={userId => this.changeUserRoom(userId, 0)}
      />
    );
  }

  renderFreeJoinCheck() {
    const { intl } = this.props;
    return (
      <label htmlFor="freeJoinCheckbox" className={styles.freeJoinLabel}>
        <input
          type="checkbox"
          className={styles.freeJoinCheckbox}
          onChange={this.setFreeJoin}
          checked={this.state.freeJoin}
        />
        {intl.formatMessage(intlMessages.freeJoinLabel)}
      </label>
    );
  }

  renderUserItemByRoom(room) {
    const dragStart = (ev) => {
      ev.dataTransfer.setData('text', ev.target.id);
      this.setState({ seletedId: ev.target.id });

      if (!this.state.valid) {
        this.setState({ valid: true });
      }
    };


    const dragEnd = () => {
      this.setState({ seletedId: '' });
    };

    return this.getUserByRoom(room)
      .map(user => (
        <p
          id={user.userId}
          key={user.userId}
          className={cx(
            styles.roomUserItem,
            this.state.seletedId === user.userId ? styles.selectedItem : null,
            )
          }
          draggable
          onDragStart={dragStart}
          onDragEnd={dragEnd}
        >
          {user.userName}
        </p>));
  }

  renderRoomSortList() {
    const { intl } = this.props;
    const { numberOfRooms } = this.state;
    const onClick = roomNumber => this.setState({ formFillLevel: 3, roomSelected: roomNumber });
    return (
      <div className={styles.listContainer}>
        <span>
          {
            new Array(numberOfRooms).fill(1).map((room, idx) => (
              <div className={styles.roomItem}>
                <h2 className={styles.itemTitle}>
                  {intl.formatMessage(intlMessages.breakoutRoomLabel, { 0: idx + 1 })}
                </h2>
                <Button
                  className={styles.itemButton}
                  label={intl.formatMessage(intlMessages.addParticipantLabel)}
                  size="lg"
                  ghost
                  color="primary"
                  onClick={() => onClick(idx + 1)}
                />
              </div>
            ))
          }
        </span>
        {this.renderButtonSetLevel(1, intl.formatMessage(intlMessages.backLabel))}
      </div>
    );
  }

  renderDesktop() {
    return [
      this.renderBreakoutForm(),
      this.renderRoomsGrid(),
    ];
  }

  renderMobile() {
    const { intl } = this.props;
    const { formFillLevel } = this.state;
    if (formFillLevel === 2) {
      return this.renderRoomSortList();
    }

    if (formFillLevel === 3) {
      return this.renderSelectUserScreen();
    }

    return [
      this.renderBreakoutForm(),
      this.renderButtonSetLevel(2, intl.formatMessage(intlMessages.nextLabel)),
    ];
  }

  renderButtonSetLevel(level, label) {
    return (
      <Button
        color="primary"
        size="lg"
        label={label}
        onClick={() => this.setState({ formFillLevel: level })}
      />
    );
  }

  render() {
    const { intl } = this.props;

    const BROWSER_RESULTS = browser();
    const isMobileBrowser = BROWSER_RESULTS.mobile ||
      BROWSER_RESULTS.os.includes('Android');

    return (
      <Modal
        title={intl.formatMessage(intlMessages.breakoutRoomTitle)}
        confirm={
          {
            label: intl.formatMessage(intlMessages.confirmButton),
            callback: this.onCreateBreakouts,
          }
        }
        dismiss={{
          callback: this.handleDismiss,
          label: intl.formatMessage(intlMessages.dismissLabel),
        }}
        preventClosing={this.state.preventClosing}
      >
        <div className={styles.content}>
          <p className={styles.subTitle}>
            {intl.formatMessage(intlMessages.breakoutRoomDesc)}
          </p>
          {isMobileBrowser ?
            this.renderMobile() : this.renderDesktop()}
        </div>
      </Modal >
    );
  }
}

export default withModalMounter(injectIntl(BreakoutRoom));
