import React from 'react';

export default class DeskshareComponent extends React.Component {
  render() {
    return (
      <video id="webcam" style={{ height: '100%', width: '100%', }} controls/>
    );
  }
};
