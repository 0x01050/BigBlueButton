import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import Button from '/imports/ui/components/button/component';
import { defineMessages, intlShape, injectIntl } from 'react-intl';
import { styles } from './styles';

const intlMessages = defineMessages({
  confirmLabel: {
    id: 'app.audioModal.yes',
    description: 'Hear yourself yes',
  },
  confirmAriaLabel: {
    id: 'app.audioModal.yes.arialabel',
    description: 'provides better context for yes btn label',
  },
});

const propTypes = {
  handleAllowAutoplay: PropTypes.func.isRequired,
  intl: intlShape.isRequired,
};

class AudioAutoplayPrompt extends PureComponent {
  render() {
    const {
      intl,
      handleAllowAutoplay,
    } = this.props;
    return (
      <span className={styles.autoplayPrompt}>
        <Button
          className={styles.button}
          label={intl.formatMessage(intlMessages.confirmLabel)}
          aria-label={intl.formatMessage(intlMessages.confirmAriaLabel)}
          icon="thumbs_up"
          circle
          color="success"
          size="jumbo"
          onClick={handleAllowAutoplay}
        />
      </span>
    );
  }
}

export default injectIntl(AudioAutoplayPrompt);

AudioAutoplayPrompt.propTypes = propTypes;
