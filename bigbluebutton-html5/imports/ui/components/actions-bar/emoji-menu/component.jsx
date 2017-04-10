import React, { Component, PropTypes } from 'react';
import { defineMessages, injectIntl } from 'react-intl';

import Button from '/imports/ui/components/button/component';
import Dropdown from '/imports/ui/components/dropdown/component';
import DropdownTrigger from '/imports/ui/components/dropdown/trigger/component';
import DropdownContent from '/imports/ui/components/dropdown/content/component';
import DropdownList from '/imports/ui/components/dropdown/list/component';
import DropdownListItem from '/imports/ui/components/dropdown/list/item/component';
import DropdownListSeparator from '/imports/ui/components/dropdown/list/separator/component';

const propTypes = {
  // Emoji status of the current user
  userEmojiStatus: PropTypes.string.isRequired,
  actions: PropTypes.object.isRequired,
};

class EmojiMenu extends Component {
  constructor(props) {
    super(props);
  }

  render() {
    const {
     userEmojiStatus,
     actions,
     intl,
   } = this.props;

    return (
      <Dropdown ref="dropdown">
        <DropdownTrigger>
          <Button
            role="button"
            label={intl.formatMessage(intlMessages.statusTriggerLabel)}
            icon="hand"
            ghost={false}
            circle={true}
            hideLabel={false}
            color="primary"
            size="lg"

            // FIXME: Without onClick react proptypes keep warning
            // even after the DropdownTrigger inject an onClick handler
            onClick={() => null}
          />
        </DropdownTrigger>
        <DropdownContent placement="top left">
          <DropdownList>
            <DropdownListItem
              icon="hand"
              label={intl.formatMessage(intlMessages.raiseLabel)}
              description={intl.formatMessage(intlMessages.raiseDesc)}
              onClick={() => actions.setEmojiHandler('hand')}
            />
            <DropdownListItem
              icon="happy"
              label={intl.formatMessage(intlMessages.happyLabel)}
              description={intl.formatMessage(intlMessages.happyDesc)}
              onClick={() => actions.setEmojiHandler('happy')}
            />
            <DropdownListItem
              icon="undecided"
              label={intl.formatMessage(intlMessages.undecidedLabel)}
              description={intl.formatMessage(intlMessages.undecidedDesc)}
              onClick={() => actions.setEmojiHandler('undecided')}
            />
            <DropdownListItem
              icon="sad"
              label={intl.formatMessage(intlMessages.sadLabel)}
              description={intl.formatMessage(intlMessages.sadDesc)}
              onClick={() => actions.setEmojiHandler('sad')}
            />
            <DropdownListItem
              icon="confused"
              label={intl.formatMessage(intlMessages.confusedLabel)}
              description={intl.formatMessage(intlMessages.confusedDesc)}
              onClick={() => actions.setEmojiHandler('confused')}
            />
            <DropdownListItem
              icon="time"
              label={intl.formatMessage(intlMessages.awayLabel)}
              description={intl.formatMessage(intlMessages.awayDesc)}
              onClick={() => actions.setEmojiHandler('time')}
            />
            <DropdownListItem
              icon="thumbs_up"
              label={intl.formatMessage(intlMessages.thumbsupLabel)}
              description={intl.formatMessage(intlMessages.thumbsupDesc)}
              onClick={() => actions.setEmojiHandler('thumbs_up')}
            />
            <DropdownListItem
              icon="thumbs_down"
              label={intl.formatMessage(intlMessages.thumbsdownLabel)}
              description={intl.formatMessage(intlMessages.thumbsdownDesc)}
              onClick={() => actions.setEmojiHandler('thumbs_down')}
            />
            <DropdownListItem
              icon="applause"
              label={intl.formatMessage(intlMessages.applauseLabel)}
              description={intl.formatMessage(intlMessages.applauseDesc)}
              onClick={() => actions.setEmojiHandler('applause')}
            />
            <DropdownListSeparator />
            <DropdownListItem
              icon="clear_status"
              label={intl.formatMessage(intlMessages.clearLabel)}
              description={intl.formatMessage(intlMessages.clearDesc)}
              onClick={() => actions.setEmojiHandler('none')}
            />
          </DropdownList>
        </DropdownContent>
      </Dropdown>
    );
  }
}

const intlMessages = defineMessages({
  statusTriggerLabel: {
    id: 'app.actionsBar.emojiMenu.statusTriggerLabel',
    description: 'Emoji status button label',
  },
  awayLabel: {
    id: 'app.actionsBar.emojiMenu.awayLabel',
    description: 'Away emoji label',
  },
  awayDesc: {
    id: 'app.actionsBar.emojiMenu.awayDesc',
    description: 'Describes awayLabel',
  },
  raiseLabel: {
    id: 'app.actionsBar.emojiMenu.raiseLabel',
    description: 'raise hand emoji label',
  },
  raiseDesc: {
    id: 'app.actionsBar.emojiMenu.raiseDesc',
    description: 'Describes raiseLabel',
  },
  undecidedLabel: {
    id: 'app.actionsBar.emojiMenu.undecidedLabel',
    description: 'undecided emoji label',
  },
  undecidedDesc: {
    id: 'app.actionsBar.emojiMenu.undecidedDesc',
    description: 'adds descriptive context to undecidedLabel',
  },
  confusedLabel: {
    id: 'app.actionsBar.emojiMenu.confusedLabel',
    description: 'confused emoji label',
  },
  confusedDesc: {
    id: 'app.actionsBar.emojiMenu.confusedDesc',
    description: 'adds descriptive context to confusedLabel',
  },
  sadLabel: {
    id: 'app.actionsBar.emojiMenu.sadLabel',
    description: 'sad emoji label',
  },
  sadDesc: {
    id: 'app.actionsBar.emojiMenu.sadDesc',
    description: 'adds descriptive context to sadLabel',
  },
  happyLabel: {
    id: 'app.actionsBar.emojiMenu.happyLabel',
    description: 'happy emoji label',
  },
  happyDesc: {
    id: 'app.actionsBar.emojiMenu.happyDesc',
    description: 'adds descriptive context to happyLabel',
  },
  clearLabel: {
    id: 'app.actionsBar.emojiMenu.clearLabel',
    description: 'confused emoji label',
  },
  clearDesc: {
    id: 'app.actionsBar.emojiMenu.clearDesc',
    description: 'adds descriptive context to clearLabel',
  },
  applauseLabel: {
    id: 'app.actionsBar.emojiMenu.applauseLabel',
    description: 'applause emoji label',
  },
  applauseDesc: {
    id: 'app.actionsBar.emojiMenu.applauseDesc',
    description: 'adds descriptive context to applauseLabel',
  },
  thumbsupLabel: {
    id: 'app.actionsBar.emojiMenu.thumbsupLabel',
    description: 'thumbs up emoji label',
  },
  thumbsupDesc: {
    id: 'app.actionsBar.emojiMenu.thumbsupDesc',
    description: 'adds descriptive context to thumbsupLabel',
  },
  thumbsdownLabel: {
    id: 'app.actionsBar.emojiMenu.thumbsdownLabel',
    description: 'thumbs down emoji label',
  },
  thumbsdownDesc: {
    id: 'app.actionsBar.emojiMenu.thumbsdownDesc',
    description: 'adds descriptive context to thumbsdownLabel',
  },
  changeStatusLabel: {
    id: 'app.actionsBar.changeStatusLabel',
    description: 'Aria-label for emoji status button',
  },
});

EmojiMenu.propTypes = propTypes;
export default injectIntl(EmojiMenu);
