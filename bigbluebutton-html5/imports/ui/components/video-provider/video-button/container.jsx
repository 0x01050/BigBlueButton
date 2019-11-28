import React from 'react';
import { withTracker } from 'meteor/react-meteor-data';
import { injectIntl } from 'react-intl';
import { withModalMounter } from '/imports/ui/components/modal/service';
import VideoPreviewContainer from '/imports/ui/components/video-preview/container';
import JoinVideoButton from './component';
import VideoService from '../service';

const JoinVideoOptionsContainer = (props) => {
  const {
    hasVideoStream,
    isDisabled,
    handleJoinVideo,
    handleCloseVideo,
    intl,
    mountModal,
    ...restProps
  } = props;

  const mountPreview = () => { mountModal(<VideoPreviewContainer fromInterface />); };

  return (
    <JoinVideoButton {...{
      handleJoinVideo: mountPreview, handleCloseVideo, hasVideoStream, isDisabled, ...restProps,
    }}
    />
  );
};

export default withModalMounter(injectIntl(withTracker(() => ({
  hasVideoStream: VideoService.hasVideoStream(),
  isDisabled: VideoService.isDisabled() || !Meteor.status().connected,
}))(JoinVideoOptionsContainer)));
