import React from 'react';
import { createContainer } from 'meteor/react-meteor-data';
import { withModalMounter } from '/imports/ui/components/modal/service';
import PropTypes from 'prop-types';
import Service from './service';
import Audio from './component';
import AudioModalContainer from './audio-modal/container';

const propTypes = {
  children: PropTypes.element,
};

const defaultProps = {
  children: null,
};

const AudioContainer = props =>
  (<Audio {...props}>
    {props.children}
  </Audio>
  );

let didMountAutoJoin = false;

export default withModalMounter(createContainer(({ mountModal }) => {
  const APP_CONFIG = Meteor.settings.public.app;

  const { autoJoinAudio } = APP_CONFIG;

  return {
    init: () => {
      Service.init();
      Service.changeOutputDevice(document.querySelector('#remote-media').sinkId)
      if (!autoJoinAudio || didMountAutoJoin) return;
      mountModal(<AudioModalContainer />);
      didMountAutoJoin = true;
    },
  };
}, AudioContainer));

AudioContainer.propTypes = propTypes;
AudioContainer.d
