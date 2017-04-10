import React, { Component, PropTypes } from 'react';
import { defineMessages, injectIntl } from 'react-intl';

import Button from '/imports/ui/components/button/component';
import Dropdown from '/imports/ui/components/dropdown/component';
import DropdownTrigger from '/imports/ui/components/dropdown/trigger/component';
import DropdownContent from '/imports/ui/components/dropdown/content/component';
import DropdownList from '/imports/ui/components/dropdown/list/component';
import DropdownListItem from '/imports/ui/components/dropdown/list/item/component';

const intlMessages = defineMessages({
  actionsLabel: {
    id: 'app.actionsBar.actionsDropdown.actionsLabel',
    description: 'Actions button label',
  },
  presentationLabel: {
    id: 'app.actionsBar.actionsDropdown.presentationLabel',
    description: 'Upload a presentation option label',
  },
  initPollLabel: {
    id: 'app.actionsBar.actionsDropdown.initPollLabel',
    description: 'Initiate a poll option label',
  },
  desktopShareLabel: {
    id: 'app.actionsBar.actionsDropdown.desktopShareLabel',
    description: 'Desktop Share option label',
  },
  presentationDesc: {
    id: 'app.actionsBar.actionsDropdown.presentationDesc',
    description: 'Describes presentationLabel',
  },
  initPollDesc: {
    id: 'app.actionsBar.actionsDropdown.initPollDesc',
    description: 'Describes initPollLabel',
  },
  desktopShareDesc: {
    id: 'app.actionsBar.actionsDropdown.desktopShareDesc',
    description: 'Describes desktopShareLabel',
  },
});

const presentation = () => {console.log('Should show the uploader component');};

const polling = () => {console.log('Should initiate a polling');};

const shareScreen = () => {console.log('Should start screen sharing');};

class ActionsDropdown extends Component {
  constructor(props) {
    super(props);
  }

  render() {
    const { intl } = this.props;
    return (
      <Dropdown ref="dropdown">
        <DropdownTrigger>
          <Button
            label={intl.formatMessage(intlMessages.actionsLabel)}
            icon="add"
            color="primary"
            size="lg"
            circle={true}
            onClick={() => null}
          />
        </DropdownTrigger>
        <DropdownContent placement="top left">
          <DropdownList>
            <DropdownListItem
              icon="presentation"
              label={intl.formatMessage(intlMessages.presentationLabel)}
              description={intl.formatMessage(intlMessages.presentationDesc)}
              onClick={presentation.bind(this)}
            />

            {/* These icons are unaligned because of the font issue
                Check it later */}
            <DropdownListItem
              icon="polling"
              label={intl.formatMessage(intlMessages.initPollLabel)}
              description={intl.formatMessage(intlMessages.initPollDesc)}
              onClick={polling.bind(this)}
            />
            <DropdownListItem
              icon="desktop"
              label={intl.formatMessage(intlMessages.desktopShareLabel)}
              description={intl.formatMessage(intlMessages.desktopShareDesc)}
              onClick={shareScreen.bind(this)}
            />
          </DropdownList>
        </DropdownContent>
      </Dropdown>
    );
  }
}

export default injectIntl(ActionsDropdown);
