import React from 'react';
import PropTypes from 'prop-types';
import { defineMessages, injectIntl, intlShape } from 'react-intl';
import browser from 'browser-detect';
import Button from '/imports/ui/components/button/component';
import { styles } from '../styles';

const propTypes = {
  intl: intlShape.isRequired,
  isUserPresenter: PropTypes.bool.isRequired,
};

const intlMessages = defineMessages({
  desktopShareLabel: {
    id: 'app.actionsBar.actionsDropdown.desktopShareLabel',
    description: 'Desktop Share option label',
  },
  stopDesktopShareLabel: {
    id: 'app.actionsBar.actionsDropdown.stopDesktopShareLabel',
    description: 'Stop Desktop Share option label',
  },
  desktopShareDesc: {
    id: 'app.actionsBar.actionsDropdown.desktopShareDesc',
    description: 'adds context to desktop share option',
  },
  stopDesktopShareDesc: {
    id: 'app.actionsBar.actionsDropdown.stopDesktopShareDesc',
    description: 'adds context to stop desktop share option',
  },

});

const BROWSER_RESULTS = browser();
const isMobileBrowser = BROWSER_RESULTS.mobile ||
  BROWSER_RESULTS.os.includes('Android'); // mobile flag doesn't always work

const DesktopShare = ({
  intl,
  handleShareScreen,
  handleUnshareScreen,
  isVideoBroadcasting,
  isUserPresenter,
}) => (
  (Meteor.settings.public.kurento.enableScreensharing &&
      !isMobileBrowser && isUserPresenter ?
        <Button
          className={styles.button}
          icon="desktop"
          label={intl.formatMessage(isVideoBroadcasting ?
          intlMessages.stopDesktopShareLabel : intlMessages.desktopShareLabel)}
          description={intl.formatMessage(isVideoBroadcasting ?
          intlMessages.stopDesktopShareDesc : intlMessages.desktopShareDesc)}
          color="primary"
          icon="desktop"
          ghost={false}
          hideLabel
          circle
          size="lg"
          onClick={isVideoBroadcasting ? handleUnshareScreen : handleShareScreen}
        />
    : null)
);

DesktopShare.propTypes = propTypes;
export default injectIntl(DesktopShare);
