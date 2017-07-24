import Screenshare from '/imports/api/2.0/screenshare';
import VertoBridge from '/imports/api/2.0/screenshare/client/bridge/verto';
import PresentationService from '/imports/ui/components/presentation/service';

const vertoBridge = new VertoBridge();

// when the meeting information has been updated check to see if it was
// screensharing. If it has changed either trigger a call to receive video
// and display it, or end the call and hide the video
function isVideoBroadcasting() {
  const ds = Screenshare.findOne({});
  // if (ds && ds.broadcast.stream) {
  //   return true;
  // }

  return (ds && ds.broadcast.stream && !PresentationService.isPresenter());
}

// if remote screenshare has been ended disconnect and hide the video stream
function presenterScreenshareHasEnded() {
  // references a function in the global namespace inside verto_extension.js
  // that we load dynamically
  vertoBridge.vertoExitVideo();
}

// if remote screenshare has been started connect and display the video stream
function presenterScreenshareHasStarted() {
  // references a function in the global namespace inside verto_extension.js
  // that we load dynamically
  vertoBridge.vertoWatchVideo();
}

export {
  isVideoBroadcasting, presenterScreenshareHasEnded, presenterScreenshareHasStarted,
};

