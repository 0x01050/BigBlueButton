import React from 'react';
import { defineMessages, injectIntl } from 'react-intl';
import Button from '/imports/ui/components/button/component';
import { styles } from './styles';

const intlMessages = defineMessages({
  title: {
    id: 'app.error.fallback.presentation.title',
    description: 'title for presentation when fallback is showed',
  },
  description: {
    id: 'app.error.fallback.presentation.description',
    description: 'description for presentation when fallback is showed',
  },
  reloadButton: {
    id: 'app.error.fallback.presentation.reloadButton',
    description: 'Button label when fallback is showed',
  },
});

const FallbackPresentation = ({ error, intl }) => (
  <div className={styles.background}>
    <h1 className={styles.codeError}>
      {intl.formatMessage(intlMessages.title)}
    </h1>
    <h1 className={styles.message}>
      {intl.formatMessage(intlMessages.description)}
    </h1>
    <div className={styles.separator} />
    <div className={styles.sessionMessage}>
      {error.message}
    </div>
    <div>
      <Button
        size="sm"
        color="primary"
        className={styles.button}
        onClick={() => window.location.reload()}
        label={intl.formatMessage(intlMessages.reloadButton)}
      />
    </div>
  </div>
);

export default injectIntl(FallbackPresentation);
