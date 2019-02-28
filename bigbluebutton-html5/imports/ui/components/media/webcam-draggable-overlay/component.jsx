import React, { Component, Fragment } from 'react';
import PropTypes from 'prop-types';
import cx from 'classnames';
import VideoProviderContainer from '/imports/ui/components/video-provider/container';
import _ from 'lodash';

import Draggable from 'react-draggable';

import { styles } from '../styles';

const propTypes = {
  floatingOverlay: PropTypes.bool,
  hideOverlay: PropTypes.bool,
};

const defaultProps = {
  floatingOverlay: false,
  hideOverlay: true,
};

export default class WebcamDraggableOverlay extends Component {
  static getWebcamBySelector() {
    return document.querySelector('video[class^="media"]');
  }

  static getIsOverlayChanged() {
    const overlayToTop = document.querySelector('div[class*="overlayToTop"]');
    const overlayToBottom = document.querySelector('div[class*="overlayToBottom"]');

    return !!(overlayToTop || overlayToBottom);
  }

  constructor(props) {
    super(props);

    this.state = {
      dragging: false,
      showDropZones: false,
      showBgDropZoneTop: false,
      showBgDropZoneBottom: false,
      dropOnTop: false,
      dropOnBottom: true,
      initialPosition: { x: 0, y: 0 },
      initialRectPosition: { x: 0, y: 0 },
      lastPosition: { x: 0, y: 0 },
      lastPositionInitSet: false,
      resetPosition: false,
      isFullScreen: false,
    };

    this.updateWebcamPositionByResize = this.updateWebcamPositionByResize.bind(this);

    this.eventResizeListener = _.throttle(
      this.updateWebcamPositionByResize,
      500,
      {
        leading: true,
        trailing: true,
      },
    );

    this.handleWebcamDragStart = this.handleWebcamDragStart.bind(this);
    this.handleWebcamDragStop = this.handleWebcamDragStop.bind(this);
    this.handleFullscreenChange = this.handleFullscreenChange.bind(this);
    this.fullscreenButtonChange = this.fullscreenButtonChange.bind(this);

    this.setIsFullScreen = this.setIsFullScreen.bind(this);
    this.setResetPosition = this.setResetPosition.bind(this);
    this.setInitialReferencePoint = this.setInitialReferencePoint.bind(this);
    this.setLastPosition = this.setLastPosition.bind(this);
    this.setLastPositionInitSet = this.setLastPositionInitSet.bind(this);
    this.setPositionAfterDropInEdge = this.setPositionAfterDropInEdge.bind(this);


    this.getLastWebcamPosition = this.getLastWebcamPosition.bind(this);

    this.dropZoneTopEnterHandler = this.dropZoneTopEnterHandler.bind(this);
    this.dropZoneTopLeaveHandler = this.dropZoneTopLeaveHandler.bind(this);

    this.dropZoneBottomEnterHandler = this.dropZoneBottomEnterHandler.bind(this);
    this.dropZoneBottomLeaveHandler = this.dropZoneBottomLeaveHandler.bind(this);

    this.dropZoneTopMouseUpHandler = this.dropZoneTopMouseUpHandler.bind(this);
    this.dropZoneBottomMouseUpHandler = this.dropZoneBottomMouseUpHandler.bind(this);
  }

  componentDidMount() {
    const { floatingOverlay } = this.props;
    const { resetPosition } = this.state;

    if (!floatingOverlay
      && (!resetPosition)) this.setResetPosition(true);

    window.addEventListener('resize', this.eventResizeListener);

    const fullscreenChangedEvents = [
      'fullscreenchange',
      'webkitfullscreenchange',
      'mozfullscreenchange',
      'MSFullscreenChange',
    ];

    fullscreenChangedEvents.forEach((event) => {
      document.addEventListener(event, this.handleFullscreenChange);
    });

    // Ensures that the event will be called before the resize
    document.addEventListener('webcamFullscreenButtonChange', this.fullscreenButtonChange);
  }

  componentDidUpdate() {
    const {
      initialRectPosition,
      lastPositionInitSet,
      dropOnTop,
      dropOnBottom,
    } = this.state;

    const { x: initX, y: initY } = initialRectPosition;

    if (initX === 0 && initY === 0) {
      this.setInitialReferencePoint();
    }

    if (initX !== 0
      && (dropOnTop || dropOnBottom)
      && !lastPositionInitSet) {
      this.setPositionAfterDropInEdge();
      this.setLastPositionInitSet(true);
    }
  }

  componentWillUnmount() {
    const fullscreenChangedEvents = [
      'fullscreenchange',
      'webkitfullscreenchange',
      'mozfullscreenchange',
      'MSFullscreenChange',
    ];

    fullscreenChangedEvents.forEach((event) => {
      document.removeEventListener(event, this.fullScreenToggleCallback);
    });

    document.removeEventListener('webcamFullscreenButtonChange', this.fullscreenButtonChange);
  }

  setIsFullScreen(isFullScreen) {
    this.setState({ isFullScreen });
  }

  setResetPosition(resetPosition) {
    this.setState({ resetPosition });
  }

  setLastPosition(x, y) {
    this.setState({ lastPosition: { x, y } }, () => console.warn('lastPosition', this.state.lastPosition));
  }

  setLastPositionInitSet(lastPositionInitSet) {
    this.setState({ lastPositionInitSet });
  }

  setInitialReferencePoint() {
    const { refMediaContainer } = this.props;

    const webcamBySelector = WebcamDraggableOverlay.getWebcamBySelector();

    if (webcamBySelector && refMediaContainer) {
      const webcamBySelectorRect = webcamBySelector.getBoundingClientRect();
      const {
        width: webcamWidth,
        height: webcamHeight,
      } = webcamBySelectorRect;

      const refMediaContainerRect = refMediaContainer.getBoundingClientRect();
      const {
        width: mediaWidth,
        height: mediaHeight,
      } = refMediaContainerRect;

      const x = mediaWidth - (webcamWidth + 10); // 10 is margin
      const y = mediaHeight - (webcamHeight + 10); // 10 is margin

      this.setState({ initialRectPosition: { x, y } });
    }
  }

  setPositionAfterDropInEdge() {
    const changed = setInterval(() => {
      const lastWebcamPosition = this.getLastWebcamPosition();
      const isOverlayChanged = WebcamDraggableOverlay.getIsOverlayChanged();

      // Wait for the element render at relative position
      if (lastWebcamPosition && isOverlayChanged) {
        const { x, y } = lastWebcamPosition;
        this.setLastPosition(x, y);
        clearInterval(changed);
      }
    }, 500);
  }

  getLastWebcamPosition() {
    const { refMediaContainer } = this.props;
    const { initialRectPosition } = this.state;

    const { x: initX, y: initY } = initialRectPosition;

    const webcamBySelector = WebcamDraggableOverlay.getWebcamBySelector();

    if (webcamBySelector && refMediaContainer) {
      const webcamBySelectorRect = webcamBySelector.getBoundingClientRect();
      const {
        left: webcamLeft,
        top: webcamTop,
      } = webcamBySelectorRect;

      const refMediaContainerRect = refMediaContainer.getBoundingClientRect();
      const {
        left: mediaLeft,
        top: mediaTop,
      } = refMediaContainerRect;

      const webcamXByMedia = (webcamLeft) - mediaLeft;
      const webcamYByMedia = (webcamTop) - mediaTop;

      const newPosition = {
        x: (webcamXByMedia - initX),
        y: (webcamYByMedia - initY),
      };

      return newPosition;
    }

    return null;
  }

  fullscreenButtonChange() {
    console.log('fullscreenButtonChange:  isfullscreen');
    this.setIsFullScreen(true);
  }

  updateWebcamPositionByResize() {
    console.warn('======== updateWebcamPositionByResize');

    // update de referencial point
    this.setInitialReferencePoint();

    const { refMediaContainer } = this.props;
    const webcamBySelector = WebcamDraggableOverlay.getWebcamBySelector();
    const {
      isFullScreen,
      lastPosition,
      initialRectPosition,
    } = this.state;
    const { x: initRectX, y: initRectY } = initialRectPosition;
    const { x: lastX, y: lastY } = lastPosition;

    if (isFullScreen) return;

    if (webcamBySelector && refMediaContainer) {
      const webcamBySelectorRect = webcamBySelector.getBoundingClientRect();
      const {
        left: webcamLeft,
        top: webcamTop,
      } = webcamBySelectorRect;

      const refMediaContainerRect = refMediaContainer.getBoundingClientRect();
      const {
        left: mediaLeft,
        top: mediaTop,
      } = refMediaContainerRect;

      const webcamXByMedia = (webcamLeft) - mediaLeft;
      const webcamYByMedia = (webcamTop) - mediaTop;

      const newX = (initRectX - 10) <= 0 ? 0 : -(initRectX - 10);
      const newY = (initRectY - 10) <= 0 ? 0 : -(initRectY - 10);

      this.setLastPosition(
        webcamXByMedia <= 0 ? newX : lastX,
        webcamYByMedia <= 0 ? newY : lastY,
      );
    }
  }

  handleFullscreenChange() {
    if (document.fullscreenElement
      || document.webkitFullscreenElement
      || document.mozFullScreenElement
      || document.msFullscreenElement) {
      window.removeEventListener('resize', this.eventResizeListener);
      this.setIsFullScreen(true);
      console.log('is FullScreen');
    } else {
      this.setIsFullScreen(false);
      window.addEventListener('resize', this.eventResizeListener);
      console.log('is not FullScreen');
    }
  }

  handleWebcamDragStart() {
    const {
      dragging,
      showDropZones,
      dropOnTop,
      dropOnBottom,
      resetPosition,
    } = this.state;

    if (!dragging) this.setState({ dragging: true });

    if (!showDropZones) this.setState({ showDropZones: true });

    if (dropOnTop) this.setState({ dropOnTop: false });
    if (dropOnBottom) this.setState({ dropOnBottom: false });
    if (resetPosition) this.setState({ resetPosition: false });

    window.dispatchEvent(new Event('resize'));
  }

  handleWebcamDragStop(e, position) {
    const {
      dragging,
      showDropZones,
    } = this.state;

    const { x, y } = position;

    if (dragging) this.setState({ dragging: false });
    if (showDropZones) this.setState({ showDropZones: false });

    this.setLastPosition(x, y);
    window.dispatchEvent(new Event('resize'));
  }

  dropZoneTopEnterHandler() {
    const {
      showBgDropZoneTop,
    } = this.state;

    if (!showBgDropZoneTop) this.setState({ showBgDropZoneTop: true });
  }

  dropZoneBottomEnterHandler() {
    const {
      showBgDropZoneBottom,
    } = this.state;

    if (!showBgDropZoneBottom) this.setState({ showBgDropZoneBottom: true });
  }

  dropZoneTopLeaveHandler() {
    const {
      showBgDropZoneTop,
    } = this.state;

    if (showBgDropZoneTop) this.setState({ showBgDropZoneTop: false });
  }

  dropZoneBottomLeaveHandler() {
    const {
      showBgDropZoneBottom,
    } = this.state;

    if (showBgDropZoneBottom) this.setState({ showBgDropZoneBottom: false });
  }

  dropZoneTopMouseUpHandler() {
    window.dispatchEvent(new Event('resize'));

    const { dropOnTop } = this.state;

    if (!dropOnTop) {
      this.setState({
        dropOnTop: true,
        resetPosition: true,
      });
    }

    this.setPositionAfterDropInEdge();
  }

  dropZoneBottomMouseUpHandler() {
    window.dispatchEvent(new Event('resize'));

    const { dropOnBottom } = this.state;

    if (!dropOnBottom) {
      this.setState({
        dropOnBottom: true,
        resetPosition: true,
      });
    }

    this.setPositionAfterDropInEdge();
  }

  render() {
    const {
      swapLayout,
      floatingOverlay,
      hideOverlay,
      disableVideo,
    } = this.props;

    const {
      dragging,
      showDropZones,
      showBgDropZoneTop,
      showBgDropZoneBottom,
      dropOnTop,
      dropOnBottom,
      initialPosition,
      lastPosition,
      resetPosition,
      isFullScreen,
    } = this.state;

    const contentClassName = cx({
      [styles.content]: true,
    });

    const overlayClassName = cx({
      [styles.overlay]: true,
      [styles.hideOverlay]: hideOverlay,
      [styles.floatingOverlay]: floatingOverlay && (!dropOnTop && !dropOnBottom),
      [styles.overlayToTop]: dropOnTop,
      [styles.overlayToBottom]: dropOnBottom,
      [styles.dragging]: dragging,
    });

    const dropZoneTopClassName = cx({
      [styles.dropZoneTop]: true,
      [styles.show]: showDropZones,
      [styles.hide]: !showDropZones,
    });

    const dropZoneBottomClassName = cx({
      [styles.dropZoneBottom]: true,
      [styles.show]: showDropZones,
      [styles.hide]: !showDropZones,
    });

    const dropZoneBgTopClassName = cx({
      [styles.dropZoneBg]: true,
      [styles.top]: true,
      [styles.show]: showBgDropZoneTop,
      [styles.hide]: !showBgDropZoneTop,
    });

    const dropZoneBgBottomClassName = cx({
      [styles.dropZoneBg]: true,
      [styles.bottom]: true,
      [styles.show]: showBgDropZoneBottom,
      [styles.hide]: !showBgDropZoneBottom,
    });

    return (
      <Fragment>
        <div
          className={dropZoneTopClassName}
          onMouseEnter={this.dropZoneTopEnterHandler}
          onMouseLeave={this.dropZoneTopLeaveHandler}
          onMouseUp={this.dropZoneTopMouseUpHandler}
          role="presentation"
          style={{ height: '100px' }}
          ref={(ref) => { this.refDropZone = ref; }}
        />
        <div
          className={dropZoneBgTopClassName}
          style={{ height: '100px' }}
        />

        <Draggable
          handle="video"
          bounds=".container"
          onStart={this.handleWebcamDragStart}
          onStop={this.handleWebcamDragStop}
          disabled={swapLayout || isFullScreen}
          position={resetPosition || swapLayout ? initialPosition : lastPosition}
        >
          <div
            className={!swapLayout ? overlayClassName : contentClassName}
            ref={(ref) => { this.refWebcamOverlay = ref; }}
          >
            {!disableVideo ? <VideoProviderContainer /> : null}
          </div>
        </Draggable>

        <div
          className={dropZoneBottomClassName}
          onMouseEnter={this.dropZoneBottomEnterHandler}
          onMouseLeave={this.dropZoneBottomLeaveHandler}
          onMouseUp={this.dropZoneBottomMouseUpHandler}
          role="presentation"
          style={{ height: '100px' }}
          ref={(ref) => { this.refDropZone = ref; }}
        />
        <div
          className={dropZoneBgBottomClassName}
          style={{ height: '100px' }}
        />
      </Fragment>
    );
  }
}

WebcamDraggableOverlay.propTypes = propTypes;
WebcamDraggableOverlay.defaultProps = defaultProps;
