import React, { Component, PropTypes } from 'react';
import styles from './styles.scss';

import Button from '../button/component';

export default class ActionsBar extends Component {
  constructor(props) {
    super(props);
  }

  handleClick() {
    console.log('dummy handler');
  }

  render() {
    return (
      <div className={styles.actionsbar}>
        <Button
          onClick={this.handleClick}
          label={'Actions'}
          color={'primary'}
          icon={'circle-add'}
          size={'lg'}
          circle={true}
        />
        &nbsp;
        <Button
          onClick={this.handleClick}
          label={'Mute'}
          color={'primary'}
          icon={'audio'}
          size={'lg'}
          circle={true}
        />
        &nbsp;
        <Button
          onClick={this.handleClick}
          label={'Cam Off'}
          color={'primary'}
          icon={'video-off'}
          size={'lg'}
          circle={true}
        />
        &nbsp;
        <Button
          onClick={this.handleClick}
          label={'Raise'}
          color={'primary'}
          icon={'hand'}
          size={'lg'}
          circle={true}
        />
      </div>
    );
  }
}
