import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import { defineMessages, injectIntl } from 'react-intl';
import { Meteor } from 'meteor/meteor';
import Button from '/imports/ui/components/button/component';
import logoutRouteHandler from '/imports/utils/logoutRouteHandler';
import { Session } from 'meteor/session';
import { styles } from './styles';

const intlMessages = defineMessages({
  500: {
    id: 'app.error.500',
    defaultMessage: 'Oops, something went wrong',
  },
  410: {
    id: 'app.error.410',
  },
  404: {
    id: 'app.error.404',
    defaultMessage: 'Not found',
  },
  403: {
    id: 'app.error.403',
  },
  401: {
    id: 'app.error.401',
  },
  400: {
    id: 'app.error.400',
  },
  leave: {
    id: 'app.error.leaveLabel',
    description: 'aria-label for leaving',
  },
});

const propTypes = {
  code: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number,
  ]),
};

const defaultProps = {
  code: 500,
};

class ErrorScreen extends PureComponent {
  componentDidMount() {
    Meteor.disconnect();
  }

  render() {
    const {
      intl,
      code,
      children,
    } = this.props;

    let formatedMessage = intl.formatMessage(intlMessages[defaultProps.code]);

    if (code in intlMessages) {
      formatedMessage = intl.formatMessage(intlMessages[code]);
    }

    return (
      <div className={styles.background}>
        {
          !Session.get('errorMessageDescription') || (
            <div className={styles.sessionMessage}>
              {Session.get('errorMessageDescription')}
            </div>)
        }
        <h1 className={styles.message}>
          {formatedMessage}
        </h1>
        <div className={styles.separator} />
        <h1 className={styles.codeError}>
          {code}
        </h1>
        <div>
          {children}
        </div>
      </div>
    );
  }
}

export default injectIntl(ErrorScreen);

ErrorScreen.propTypes = propTypes;
ErrorScreen.defaultProps = defaultProps;
