import React, { Component, PropTypes } from 'react';
import { createContainer } from 'meteor/react-meteor-data';

import Navbar from './Navbar.jsx';

class NavbarContainer extends Component {
  constructor(props) {
    super(props);
  }

  render() {
    return (
      <Navbar {...this.props}>
        {this.props.children}
      </Navbar>
    );
  }
}

export default createContainer(() => {
  return {
    presentationTitle: 'IMDT 1004 Design Process',
    hasUnreadMessages: true,
  };
}, NavbarContainer);
