/*
 *  Copyright (c) 2016 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree.
 */

/* More information about these options at jshint.com/docs/options */
/* jshint browser: true, camelcase: true, curly: true, devel: true,
   eqeqeq: true, forin: false, globalstrict: true, node: true,
   quotmark: single, undef: true, unused: strict */
/* global mozRTCIceCandidate, mozRTCPeerConnection, Promise,
mozRTCSessionDescription, webkitRTCPeerConnection, MediaStreamTrack,
MediaStream, RTCIceGatherer, RTCIceTransport, RTCDtlsTransport,
RTCRtpSender, RTCRtpReceiver */
/* exported trace,requestUserMedia */


let getUserMedia = null;
let attachMediaStream = null;
let reattachMediaStream = null;
let webrtcDetectedBrowser = null;
let webrtcDetectedVersion = null;
let webrtcMinimumVersion = null;
const webrtcUtils = {
  log() {
    // suppress console.log output when being included as a module.
    if (typeof module !== 'undefined' ||
        typeof require === 'function' && typeof define === 'function') {
      return;
    }
    console.log(...arguments);
  },
  extractVersion(uastring, expr, pos) {
    const match = uastring.match(expr);
    return match && match.length >= pos && parseInt(match[pos], 10);
  },
};

function trace(text) {
  // This function is used for logging.
  if (text[text.length - 1] === '\n') {
    text = text.substring(0, text.length - 1);
  }
  if (window.performance) {
    const now = (window.performance.now() / 1000).toFixed(3);
    webrtcUtils.log(`${now}: ${text}`);
  } else {
    webrtcUtils.log(text);
  }
}

if (typeof window === 'object') {
  if (window.HTMLMediaElement &&
    !('srcObject' in window.HTMLMediaElement.prototype)) {
    // Shim the srcObject property, once, when HTMLMediaElement is found.
    Object.defineProperty(window.HTMLMediaElement.prototype, 'srcObject', {
      get() {
        // If prefixed srcObject property exists, return it.
        // Otherwise use the shimmed property, _srcObject
        return 'mozSrcObject' in this ? this.mozSrcObject : this._srcObject;
      },
      set(stream) {
        if ('mozSrcObject' in this) {
          this.mozSrcObject = stream;
        } else {
          // Use _srcObject as a private property for this shim
          this._srcObject = stream;
          // TODO: revokeObjectUrl(this.src) when !stream to release resources?
          this.src = URL.createObjectURL(stream);
        }
      },
    });
  }
  // Proxy existing globals
  getUserMedia = window.navigator && window.navigator.getUserMedia;
}

// Attach a media stream to an element.
attachMediaStream = function (element, stream) {
  element.srcObject = stream;
};

reattachMediaStream = function (to, from) {
  to.srcObject = from.srcObject;
};

if (typeof window === 'undefined' || !window.navigator) {
  webrtcUtils.log('This does not appear to be a browser');
  webrtcDetectedBrowser = 'not a browser';
} else if (navigator.mozGetUserMedia) {
  webrtcUtils.log('This appears to be Firefox');

  webrtcDetectedBrowser = 'firefox';

  // the detected firefox version.
  webrtcDetectedVersion = webrtcUtils.extractVersion(navigator.userAgent,
      /Firefox\/([0-9]+)\./, 1);

  // the minimum firefox version still supported by adapter.
  webrtcMinimumVersion = 31;

  // Shim for RTCPeerConnection on older versions.
  if (!window.RTCPeerConnection) {
    window.RTCPeerConnection = function (pcConfig, pcConstraints) {
      if (webrtcDetectedVersion < 38) {
        // .urls is not supported in FF < 38.
        // create RTCIceServers with a single url.
        if (pcConfig && pcConfig.iceServers) {
          const newIceServers = [];
          for (let i = 0; i < pcConfig.iceServers.length; i++) {
            const server = pcConfig.iceServers[i];
            if (server.hasOwnProperty('urls')) {
              for (let j = 0; j < server.urls.length; j++) {
                const newServer = {
                  url: server.urls[j],
                };
                if (server.urls[j].indexOf('turn') === 0) {
                  newServer.username = server.username;
                  newServer.credential = server.credential;
                }
                newIceServers.push(newServer);
              }
            } else {
              newIceServers.push(pcConfig.iceServers[i]);
            }
          }
          pcConfig.iceServers = newIceServers;
        }
      }
      return new mozRTCPeerConnection(pcConfig, pcConstraints); // jscs:ignore requireCapitalizedConstructors
    };

    // wrap static methods. Currently just generateCertificate.
    if (mozRTCPeerConnection.generateCertificate) {
      Object.defineProperty(window.RTCPeerConnection, 'generateCertificate', {
        get() {
          if (arguments.length) {
            return mozRTCPeerConnection.generateCertificate.apply(null,
                arguments);
          }
          return mozRTCPeerConnection.generateCertificate;
        },
      });
    }

    window.RTCSessionDescription = mozRTCSessionDescription;
    window.RTCIceCandidate = mozRTCIceCandidate;
  }

  // getUserMedia constraints shim.
  getUserMedia = function (constraints, onSuccess, onError) {
    const constraintsToFF37 = function (c) {
      if (typeof c !== 'object' || c.require) {
        return c;
      }
      const require = [];
      Object.keys(c).forEach((key) => {
        if (key === 'require' || key === 'advanced' || key === 'mediaSource') {
          return;
        }
        const r = c[key] = (typeof c[key] === 'object') ?
            c[key] : { ideal: c[key] };
        if (r.min !== undefined ||
            r.max !== undefined || r.exact !== undefined) {
          require.push(key);
        }
        if (r.exact !== undefined) {
          if (typeof r.exact === 'number') {
            r.min = r.max = r.exact;
          } else {
            c[key] = r.exact;
          }
          delete r.exact;
        }
        if (r.ideal !== undefined) {
          c.advanced = c.advanced || [];
          const oc = {};
          if (typeof r.ideal === 'number') {
            oc[key] = { min: r.ideal, max: r.ideal };
          } else {
            oc[key] = r.ideal;
          }
          c.advanced.push(oc);
          delete r.ideal;
          if (!Object.keys(r).length) {
            delete c[key];
          }
        }
      });
      if (require.length) {
        c.require = require;
      }
      return c;
    };
    if (webrtcDetectedVersion < 38) {
      webrtcUtils.log(`spec: ${JSON.stringify(constraints)}`);
      if (constraints.audio) {
        constraints.audio = constraintsToFF37(constraints.audio);
      }
      if (constraints.video) {
        constraints.video = constraintsToFF37(constraints.video);
      }
      webrtcUtils.log(`ff37: ${JSON.stringify(constraints)}`);
    }
    return navigator.mozGetUserMedia(constraints, onSuccess, onError);
  };

  navigator.getUserMedia = getUserMedia;

  // Shim for mediaDevices on older versions.
  if (!navigator.mediaDevices) {
    navigator.mediaDevices = { getUserMedia: requestUserMedia,
      addEventListener() { },
      removeEventListener() { },
    };
  }
  navigator.mediaDevices.enumerateDevices =
      navigator.mediaDevices.enumerateDevices || function () {
        return new Promise((resolve) => {
          const infos = [
        { kind: 'audioinput', deviceId: 'default', label: '', groupId: '' },
        { kind: 'videoinput', deviceId: 'default', label: '', groupId: '' },
          ];
          resolve(infos);
        });
      };

  if (webrtcDetectedVersion < 41) {
    // Work around http://bugzil.la/1169665
    const orgEnumerateDevices =
        navigator.mediaDevices.enumerateDevices.bind(navigator.mediaDevices);
    navigator.mediaDevices.enumerateDevices = function () {
      return orgEnumerateDevices().then(undefined, (e) => {
        if (e.name === 'NotFoundError') {
          return [];
        }
        throw e;
      });
    };
  }
} else if (navigator.webkitGetUserMedia && window.webkitRTCPeerConnection) {
  webrtcUtils.log('This appears to be Chrome');

  webrtcDetectedBrowser = 'chrome';

  // the detected chrome version.
  webrtcDetectedVersion = webrtcUtils.extractVersion(navigator.userAgent,
      /Chrom(e|ium)\/([0-9]+)\./, 2);

  // the minimum chrome version still supported by adapter.
  webrtcMinimumVersion = 38;

  // The RTCPeerConnection object.
  window.RTCPeerConnection = function (pcConfig, pcConstraints) {
    // Translate iceTransportPolicy to iceTransports,
    // see https://code.google.com/p/webrtc/issues/detail?id=4869
    if (pcConfig && pcConfig.iceTransportPolicy) {
      pcConfig.iceTransports = pcConfig.iceTransportPolicy;
    }

    const pc = new webkitRTCPeerConnection(pcConfig, pcConstraints); // jscs:ignore requireCapitalizedConstructors
    const origGetStats = pc.getStats.bind(pc);
    pc.getStats = function (selector, successCallback, errorCallback) { // jshint ignore: line
      const self = this;
      const args = arguments;

      // If selector is a function then we are in the old style stats so just
      // pass back the original getStats format to avoid breaking old users.
      if (arguments.length > 0 && typeof selector === 'function') {
        return origGetStats(selector, successCallback);
      }

      const fixChromeStats = function (response) {
        const standardReport = {};
        const reports = response.result();
        reports.forEach((report) => {
          const standardStats = {
            id: report.id,
            timestamp: report.timestamp,
            type: report.type,
          };
          report.names().forEach((name) => {
            standardStats[name] = report.stat(name);
          });
          standardReport[standardStats.id] = standardStats;
        });

        return standardReport;
      };

      if (arguments.length >= 2) {
        const successCallbackWrapper = function (response) {
          args[1](fixChromeStats(response));
        };

        return origGetStats.apply(this, [successCallbackWrapper, arguments[0]]);
      }

      // promise-support
      return new Promise((resolve, reject) => {
        if (args.length === 1 && selector === null) {
          origGetStats.apply(self, [
            function (response) {
              resolve.apply(null, [fixChromeStats(response)]);
            }, reject]);
        } else {
          origGetStats.apply(self, [resolve, reject]);
        }
      });
    };

    return pc;
  };

  // wrap static methods. Currently just generateCertificate.
  if (webkitRTCPeerConnection.generateCertificate) {
    Object.defineProperty(window.RTCPeerConnection, 'generateCertificate', {
      get() {
        if (arguments.length) {
          return webkitRTCPeerConnection.generateCertificate.apply(null,
              arguments);
        }
        return webkitRTCPeerConnection.generateCertificate;
      },
    });
  }

  // add promise support
  ['createOffer', 'createAnswer'].forEach((method) => {
    const nativeMethod = webkitRTCPeerConnection.prototype[method];
    webkitRTCPeerConnection.prototype[method] = function () {
      const self = this;
      if (arguments.length < 1 || (arguments.length === 1 &&
          typeof (arguments[0]) === 'object')) {
        const opts = arguments.length === 1 ? arguments[0] : undefined;
        return new Promise((resolve, reject) => {
          nativeMethod.apply(self, [resolve, reject, opts]);
        });
      }
      return nativeMethod.apply(this, arguments);
    };
  });

  ['setLocalDescription', 'setRemoteDescription',
    'addIceCandidate'].forEach((method) => {
      const nativeMethod = webkitRTCPeerConnection.prototype[method];
      webkitRTCPeerConnection.prototype[method] = function () {
        const args = arguments;
        const self = this;
        return new Promise((resolve, reject) => {
          nativeMethod.apply(self, [args[0],
            function () {
              resolve();
              if (args.length >= 2) {
                args[1].apply(null, []);
              }
            },
            function (err) {
              reject(err);
              if (args.length >= 3) {
                args[2].apply(null, [err]);
              }
            }],
          );
        });
      };
    });

  // getUserMedia constraints shim.
  const constraintsToChrome = function (c) {
    if (typeof c !== 'object' || c.mandatory || c.optional) {
      return c;
    }
    const cc = {};
    Object.keys(c).forEach((key) => {
      if (key === 'require' || key === 'advanced' || key === 'mediaSource') {
        return;
      }
      const r = (typeof c[key] === 'object') ? c[key] : { ideal: c[key] };
      if (r.exact !== undefined && typeof r.exact === 'number') {
        r.min = r.max = r.exact;
      }
      const oldname = function (prefix, name) {
        if (prefix) {
          return prefix + name.charAt(0).toUpperCase() + name.slice(1);
        }
        return (name === 'deviceId') ? 'sourceId' : name;
      };
      if (r.ideal !== undefined) {
        cc.optional = cc.optional || [];
        let oc = {};
        if (typeof r.ideal === 'number') {
          oc[oldname('min', key)] = r.ideal;
          cc.optional.push(oc);
          oc = {};
          oc[oldname('max', key)] = r.ideal;
          cc.optional.push(oc);
        } else {
          oc[oldname('', key)] = r.ideal;
          cc.optional.push(oc);
        }
      }
      if (r.exact !== undefined && typeof r.exact !== 'number') {
        cc.mandatory = cc.mandatory || {};
        cc.mandatory[oldname('', key)] = r.exact;
      } else {
        ['min', 'max'].forEach((mix) => {
          if (r[mix] !== undefined) {
            cc.mandatory = cc.mandatory || {};
            cc.mandatory[oldname(mix, key)] = r[mix];
          }
        });
      }
    });
    if (c.advanced) {
      cc.optional = (cc.optional || []).concat(c.advanced);
    }
    return cc;
  };

  getUserMedia = function (constraints, onSuccess, onError) {
    if (constraints.audio) {
      constraints.audio = constraintsToChrome(constraints.audio);
    }
    if (constraints.video) {
      constraints.video = constraintsToChrome(constraints.video);
    }
    webrtcUtils.log(`chrome: ${JSON.stringify(constraints)}`);
    return navigator.webkitGetUserMedia(constraints, onSuccess, onError);
  };
  navigator.getUserMedia = getUserMedia;

  if (!navigator.mediaDevices) {
    navigator.mediaDevices = { getUserMedia: requestUserMedia,
      enumerateDevices() {
        return new Promise((resolve) => {
          const kinds = { audio: 'audioinput', video: 'videoinput' };
          return MediaStreamTrack.getSources((devices) => {
            resolve(devices.map(device => ({ label: device.label,
              kind: kinds[device.kind],
              deviceId: device.id,
              groupId: '' })));
          });
        });
      } };
  }

  // A shim for getUserMedia method on the mediaDevices object.
  // TODO(KaptenJansson) remove once implemented in Chrome stable.
  if (!navigator.mediaDevices.getUserMedia) {
    navigator.mediaDevices.getUserMedia = function (constraints) {
      return requestUserMedia(constraints);
    };
  } else {
    // Even though Chrome 45 has navigator.mediaDevices and a getUserMedia
    // function which returns a Promise, it does not accept spec-style
    // constraints.
    const origGetUserMedia = navigator.mediaDevices.getUserMedia
        .bind(navigator.mediaDevices);
    navigator.mediaDevices.getUserMedia = function (c) {
      webrtcUtils.log(`spec:   ${JSON.stringify(c)}`); // whitespace for alignment
      c.audio = constraintsToChrome(c.audio);
      c.video = constraintsToChrome(c.video);
      webrtcUtils.log(`chrome: ${JSON.stringify(c)}`);
      return origGetUserMedia(c);
    };
  }

  // Dummy devicechange event methods.
  // TODO(KaptenJansson) remove once implemented in Chrome stable.
  if (typeof navigator.mediaDevices.addEventListener === 'undefined') {
    navigator.mediaDevices.addEventListener = function () {
      webrtcUtils.log('Dummy mediaDevices.addEventListener called.');
    };
  }
  if (typeof navigator.mediaDevices.removeEventListener === 'undefined') {
    navigator.mediaDevices.removeEventListener = function () {
      webrtcUtils.log('Dummy mediaDevices.removeEventListener called.');
    };
  }

  // Attach a media stream to an element.
  attachMediaStream = function (element, stream) {
    if (webrtcDetectedVersion >= 43) {
      element.srcObject = stream;
    } else if (typeof element.src !== 'undefined') {
      element.src = URL.createObjectURL(stream);
    } else {
      webrtcUtils.log('Error attaching stream to element.');
    }
  };
  reattachMediaStream = function (to, from) {
    if (webrtcDetectedVersion >= 43) {
      to.srcObject = from.srcObject;
    } else {
      to.src = from.src;
    }
  };
} else if (navigator.mediaDevices && navigator.userAgent.match(
    /Edge\/(\d+).(\d+)$/)) {
  webrtcUtils.log('This appears to be Edge');
  webrtcDetectedBrowser = 'edge';

  webrtcDetectedVersion = webrtcUtils.extractVersion(navigator.userAgent,
      /Edge\/(\d+).(\d+)$/, 2);

  // The minimum version still supported by adapter.
  // This is the build number for Edge.
  webrtcMinimumVersion = 10547;

  if (window.RTCIceGatherer) {
    // Generate an alphanumeric identifier for cname or mids.
    // TODO: use UUIDs instead? https://gist.github.com/jed/982883
    const generateIdentifier = function () {
      return Math.random().toString(36).substr(2, 10);
    };

    // The RTCP CNAME used by all peerconnections from the same JS.
    const localCName = generateIdentifier();

    // SDP helpers - to be moved into separate module.
    const SDPUtils = {};

    // Splits SDP into lines, dealing with both CRLF and LF.
    SDPUtils.splitLines = function (blob) {
      return blob.trim().split('\n').map(line => line.trim());
    };

    // Splits SDP into sessionpart and mediasections. Ensures CRLF.
    SDPUtils.splitSections = function (blob) {
      const parts = blob.split('\r\nm=');
      return parts.map((part, index) => `${(index > 0 ? `m=${part}` : part).trim()}\r\n`);
    };

    // Returns lines that start with a certain prefix.
    SDPUtils.matchPrefix = function (blob, prefix) {
      return SDPUtils.splitLines(blob).filter(line => line.indexOf(prefix) === 0);
    };

    // Parses an ICE candidate line. Sample input:
    // candidate:702786350 2 udp 41819902 8.8.8.8 60769 typ relay raddr 8.8.8.8 rport 55996"
    SDPUtils.parseCandidate = function (line) {
      let parts;
      // Parse both variants.
      if (line.indexOf('a=candidate:') === 0) {
        parts = line.substring(12).split(' ');
      } else {
        parts = line.substring(10).split(' ');
      }

      const candidate = {
        foundation: parts[0],
        component: parts[1],
        protocol: parts[2].toLowerCase(),
        priority: parseInt(parts[3], 10),
        ip: parts[4],
        port: parseInt(parts[5], 10),
        // skip parts[6] == 'typ'
        type: parts[7],
      };

      for (let i = 8; i < parts.length; i += 2) {
        switch (parts[i]) {
          case 'raddr':
            candidate.relatedAddress = parts[i + 1];
            break;
          case 'rport':
            candidate.relatedPort = parseInt(parts[i + 1], 10);
            break;
          case 'tcptype':
            candidate.tcpType = parts[i + 1];
            break;
          default: // Unknown extensions are silently ignored.
            break;
        }
      }
      return candidate;
    };

    // Translates a candidate object into SDP candidate attribute.
    SDPUtils.writeCandidate = function (candidate) {
      const sdp = [];
      sdp.push(candidate.foundation);
      sdp.push(candidate.component);
      sdp.push(candidate.protocol.toUpperCase());
      sdp.push(candidate.priority);
      sdp.push(candidate.ip);
      sdp.push(candidate.port);

      const type = candidate.type;
      sdp.push('typ');
      sdp.push(type);
      if (type !== 'host' && candidate.relatedAddress &&
          candidate.relatedPort) {
        sdp.push('raddr');
        sdp.push(candidate.relatedAddress); // was: relAddr
        sdp.push('rport');
        sdp.push(candidate.relatedPort); // was: relPort
      }
      if (candidate.tcpType && candidate.protocol.toLowerCase() === 'tcp') {
        sdp.push('tcptype');
        sdp.push(candidate.tcpType);
      }
      return `candidate:${sdp.join(' ')}`;
    };

    // Parses an rtpmap line, returns RTCRtpCoddecParameters. Sample input:
    // a=rtpmap:111 opus/48000/2
    SDPUtils.parseRtpMap = function (line) {
      let parts = line.substr(9).split(' ');
      const parsed = {
        payloadType: parseInt(parts.shift(), 10), // was: id
      };

      parts = parts[0].split('/');

      parsed.name = parts[0];
      parsed.clockRate = parseInt(parts[1], 10); // was: clockrate
      parsed.numChannels = parts.length === 3 ? parseInt(parts[2], 10) : 1; // was: channels
      return parsed;
    };

    // Generate an a=rtpmap line from RTCRtpCodecCapability or RTCRtpCodecParameters.
    SDPUtils.writeRtpMap = function (codec) {
      let pt = codec.payloadType;
      if (codec.preferredPayloadType !== undefined) {
        pt = codec.preferredPayloadType;
      }
      return `a=rtpmap:${pt} ${codec.name}/${codec.clockRate
          }${codec.numChannels !== 1 ? `/${codec.numChannels}` : ''}\r\n`;
    };

    // Parses an ftmp line, returns dictionary. Sample input:
    // a=fmtp:96 vbr=on;cng=on
    // Also deals with vbr=on; cng=on
    SDPUtils.parseFmtp = function (line) {
      const parsed = {};
      let kv;
      const parts = line.substr(line.indexOf(' ') + 1).split(';');
      for (let j = 0; j < parts.length; j++) {
        kv = parts[j].trim().split('=');
        parsed[kv[0].trim()] = kv[1];
      }
      return parsed;
    };

    // Generates an a=ftmp line from RTCRtpCodecCapability or RTCRtpCodecParameters.
    SDPUtils.writeFtmp = function (codec) {
      let line = '';
      let pt = codec.payloadType;
      if (codec.preferredPayloadType !== undefined) {
        pt = codec.preferredPayloadType;
      }
      if (codec.parameters && codec.parameters.length) {
        const params = [];
        Object.keys(codec.parameters).forEach((param) => {
          params.push(`${param}=${codec.parameters[param]}`);
        });
        line += `a=fmtp:${pt} ${params.join(';')}\r\n`;
      }
      return line;
    };

    // Parses an rtcp-fb line, returns RTCPRtcpFeedback object. Sample input:
    // a=rtcp-fb:98 nack rpsi
    SDPUtils.parseRtcpFb = function (line) {
      const parts = line.substr(line.indexOf(' ') + 1).split(' ');
      return {
        type: parts.shift(),
        parameter: parts.join(' '),
      };
    };
    // Generate a=rtcp-fb lines from RTCRtpCodecCapability or RTCRtpCodecParameters.
    SDPUtils.writeRtcpFb = function (codec) {
      let lines = '';
      let pt = codec.payloadType;
      if (codec.preferredPayloadType !== undefined) {
        pt = codec.preferredPayloadType;
      }
      if (codec.rtcpFeedback && codec.rtcpFeedback.length) {
        // FIXME: special handling for trr-int?
        codec.rtcpFeedback.forEach((fb) => {
          lines += `a=rtcp-fb:${pt} ${fb.type} ${fb.parameter
              }\r\n`;
        });
      }
      return lines;
    };

    // Parses an RFC 5576 ssrc media attribute. Sample input:
    // a=ssrc:3735928559 cname:something
    SDPUtils.parseSsrcMedia = function (line) {
      const sp = line.indexOf(' ');
      const parts = {
        ssrc: line.substr(7, sp - 7),
      };
      const colon = line.indexOf(':', sp);
      if (colon > -1) {
        parts.attribute = line.substr(sp + 1, colon - sp - 1);
        parts.value = line.substr(colon + 1);
      } else {
        parts.attribute = line.substr(sp + 1);
      }
      return parts;
    };

    // Extracts DTLS parameters from SDP media section or sessionpart.
    // FIXME: for consistency with other functions this should only
    //   get the fingerprint line as input. See also getIceParameters.
    SDPUtils.getDtlsParameters = function (mediaSection, sessionpart) {
      let lines = SDPUtils.splitLines(mediaSection);
      lines = lines.concat(SDPUtils.splitLines(sessionpart)); // Search in session part, too.
      const fpLine = lines.filter(line => line.indexOf('a=fingerprint:') === 0)[0].substr(14);
      // Note: a=setup line is ignored since we use the 'auto' role.
      const dtlsParameters = {
        role: 'auto',
        fingerprints: [{
          algorithm: fpLine.split(' ')[0],
          value: fpLine.split(' ')[1],
        }],
      };
      return dtlsParameters;
    };

    // Serializes DTLS parameters to SDP.
    SDPUtils.writeDtlsParameters = function (params, setupType) {
      let sdp = `a=setup:${setupType}\r\n`;
      params.fingerprints.forEach((fp) => {
        sdp += `a=fingerprint:${fp.algorithm} ${fp.value}\r\n`;
      });
      return sdp;
    };
    // Parses ICE information from SDP media section or sessionpart.
    // FIXME: for consistency with other functions this should only
    //   get the ice-ufrag and ice-pwd lines as input.
    SDPUtils.getIceParameters = function (mediaSection, sessionpart) {
      let lines = SDPUtils.splitLines(mediaSection);
      lines = lines.concat(SDPUtils.splitLines(sessionpart)); // Search in session part, too.
      const iceParameters = {
        usernameFragment: lines.filter(line => line.indexOf('a=ice-ufrag:') === 0)[0].substr(12),
        password: lines.filter(line => line.indexOf('a=ice-pwd:') === 0)[0].substr(10),
      };
      return iceParameters;
    };

    // Serializes ICE parameters to SDP.
    SDPUtils.writeIceParameters = function (params) {
      return `a=ice-ufrag:${params.usernameFragment}\r\n` +
          `a=ice-pwd:${params.password}\r\n`;
    };

    // Parses the SDP media section and returns RTCRtpParameters.
    SDPUtils.parseRtpParameters = function (mediaSection) {
      const description = {
        codecs: [],
        headerExtensions: [],
        fecMechanisms: [],
        rtcp: [],
      };
      const lines = SDPUtils.splitLines(mediaSection);
      const mline = lines[0].split(' ');
      for (let i = 3; i < mline.length; i++) { // find all codecs from mline[3..]
        const pt = mline[i];
        const rtpmapline = SDPUtils.matchPrefix(
            mediaSection, `a=rtpmap:${pt} `)[0];
        if (rtpmapline) {
          const codec = SDPUtils.parseRtpMap(rtpmapline);
          const fmtps = SDPUtils.matchPrefix(
              mediaSection, `a=fmtp:${pt} `);
          // Only the first a=fmtp:<pt> is considered.
          codec.parameters = fmtps.length ? SDPUtils.parseFmtp(fmtps[0]) : {};
          codec.rtcpFeedback = SDPUtils.matchPrefix(
              mediaSection, `a=rtcp-fb:${pt} `)
            .map(SDPUtils.parseRtcpFb);
          description.codecs.push(codec);
        }
      }
      // FIXME: parse headerExtensions, fecMechanisms and rtcp.
      return description;
    };

    // Generates parts of the SDP media section describing the capabilities / parameters.
    SDPUtils.writeRtpDescription = function (kind, caps) {
      let sdp = '';

      // Build the mline.
      sdp += `m=${kind} `;
      sdp += caps.codecs.length > 0 ? '9' : '0'; // reject if no codecs.
      sdp += ' UDP/TLS/RTP/SAVPF ';
      sdp += `${caps.codecs.map((codec) => {
        if (codec.preferredPayloadType !== undefined) {
          return codec.preferredPayloadType;
        }
        return codec.payloadType;
      }).join(' ')}\r\n`;

      sdp += 'c=IN IP4 0.0.0.0\r\n';
      sdp += 'a=rtcp:9 IN IP4 0.0.0.0\r\n';

      // Add a=rtpmap lines for each codec. Also fmtp and rtcp-fb.
      caps.codecs.forEach((codec) => {
        sdp += SDPUtils.writeRtpMap(codec);
        sdp += SDPUtils.writeFtmp(codec);
        sdp += SDPUtils.writeRtcpFb(codec);
      });
      // FIXME: add headerExtensions, fecMechanismş and rtcp.
      sdp += 'a=rtcp-mux\r\n';
      return sdp;
    };

    SDPUtils.writeSessionBoilerplate = function () {
      // FIXME: sess-id should be an NTP timestamp.
      return 'v=0\r\n' +
          'o=thisisadapterortc 8169639915646943137 2 IN IP4 127.0.0.1\r\n' +
          's=-\r\n' +
          't=0 0\r\n';
    };

    SDPUtils.writeMediaSection = function (transceiver, caps, type, stream) {
      let sdp = SDPUtils.writeRtpDescription(transceiver.kind, caps);

      // Map ICE parameters (ufrag, pwd) to SDP.
      sdp += SDPUtils.writeIceParameters(
          transceiver.iceGatherer.getLocalParameters());

      // Map DTLS parameters to SDP.
      sdp += SDPUtils.writeDtlsParameters(
          transceiver.dtlsTransport.getLocalParameters(),
          type === 'offer' ? 'actpass' : 'active');

      sdp += `a=mid:${transceiver.mid}\r\n`;

      if (transceiver.rtpSender && transceiver.rtpReceiver) {
        sdp += 'a=sendrecv\r\n';
      } else if (transceiver.rtpSender) {
        sdp += 'a=sendonly\r\n';
      } else if (transceiver.rtpReceiver) {
        sdp += 'a=recvonly\r\n';
      } else {
        sdp += 'a=inactive\r\n';
      }

      // FIXME: for RTX there might be multiple SSRCs. Not implemented in Edge yet.
      if (transceiver.rtpSender) {
        const msid = `msid:${stream.id} ${
            transceiver.rtpSender.track.id}\r\n`;
        sdp += `a=${msid}`;
        sdp += `a=ssrc:${transceiver.sendSsrc} ${msid}`;
      }
      // FIXME: this should be written by writeRtpDescription.
      sdp += `a=ssrc:${transceiver.sendSsrc} cname:${
          localCName}\r\n`;
      return sdp;
    };

    // Gets the direction from the mediaSection or the sessionpart.
    SDPUtils.getDirection = function (mediaSection, sessionpart) {
      // Look for sendrecv, sendonly, recvonly, inactive, default to sendrecv.
      const lines = SDPUtils.splitLines(mediaSection);
      for (let i = 0; i < lines.length; i++) {
        switch (lines[i]) {
          case 'a=sendrecv':
          case 'a=sendonly':
          case 'a=recvonly':
          case 'a=inactive':
            return lines[i].substr(2);
        }
      }
      if (sessionpart) {
        return SDPUtils.getDirection(sessionpart);
      }
      return 'sendrecv';
    };

    // ORTC defines an RTCIceCandidate object but no constructor.
    // Not implemented in Edge.
    if (!window.RTCIceCandidate) {
      window.RTCIceCandidate = function (args) {
        return args;
      };
    }
    // ORTC does not have a session description object but
    // other browsers (i.e. Chrome) that will support both PC and ORTC
    // in the future might have this defined already.
    if (!window.RTCSessionDescription) {
      window.RTCSessionDescription = function (args) {
        return args;
      };
    }

    window.RTCPeerConnection = function (config) {
      const self = this;

      this.onicecandidate = null;
      this.onaddstream = null;
      this.onremovestream = null;
      this.onsignalingstatechange = null;
      this.oniceconnectionstatechange = null;
      this.onnegotiationneeded = null;
      this.ondatachannel = null;

      this.localStreams = [];
      this.remoteStreams = [];
      this.getLocalStreams = function () { return self.localStreams; };
      this.getRemoteStreams = function () { return self.remoteStreams; };

      this.localDescription = new RTCSessionDescription({
        type: '',
        sdp: '',
      });
      this.remoteDescription = new RTCSessionDescription({
        type: '',
        sdp: '',
      });
      this.signalingState = 'stable';
      this.iceConnectionState = 'new';

      this.iceOptions = {
        gatherPolicy: 'all',
        iceServers: [],
      };
      if (config && config.iceTransportPolicy) {
        switch (config.iceTransportPolicy) {
          case 'all':
          case 'relay':
            this.iceOptions.gatherPolicy = config.iceTransportPolicy;
            break;
          case 'none':
            // FIXME: remove once implementation and spec have added this.
            throw new TypeError('iceTransportPolicy "none" not supported');
        }
      }
      if (config && config.iceServers) {
        // Edge does not like
        // 1) stun:
        // 2) turn: that does not have all of turn:host:port?transport=udp
        // 3) an array of urls
        config.iceServers.forEach((server) => {
          if (server.urls) {
            let url;
            if (typeof (server.urls) === 'string') {
              url = server.urls;
            } else {
              url = server.urls[0];
            }
            if (url.indexOf('transport=udp') !== -1) {
              self.iceServers.push({
                username: server.username,
                credential: server.credential,
                urls: url,
              });
            }
          }
        });
      }

      // per-track iceGathers, iceTransports, dtlsTransports, rtpSenders, ...
      // everything that is needed to describe a SDP m-line.
      this.transceivers = [];

      // since the iceGatherer is currently created in createOffer but we
      // must not emit candidates until after setLocalDescription we buffer
      // them in this array.
      this._localIceCandidatesBuffer = [];
    };

    window.RTCPeerConnection.prototype._emitBufferedCandidates = function () {
      const self = this;
      // FIXME: need to apply ice candidates in a way which is async but in-order
      this._localIceCandidatesBuffer.forEach((event) => {
        if (self.onicecandidate !== null) {
          self.onicecandidate(event);
        }
      });
      this._localIceCandidatesBuffer = [];
    };

    window.RTCPeerConnection.prototype.addStream = function (stream) {
      // Clone is necessary for local demos mostly, attaching directly
      // to two different senders does not work (build 10547).
      this.localStreams.push(stream.clone());
      this._maybeFireNegotiationNeeded();
    };

    window.RTCPeerConnection.prototype.removeStream = function (stream) {
      const idx = this.localStreams.indexOf(stream);
      if (idx > -1) {
        this.localStreams.splice(idx, 1);
        this._maybeFireNegotiationNeeded();
      }
    };

    // Determines the intersection of local and remote capabilities.
    window.RTCPeerConnection.prototype._getCommonCapabilities =
        function (localCapabilities, remoteCapabilities) {
          const commonCapabilities = {
            codecs: [],
            headerExtensions: [],
            fecMechanisms: [],
          };
          localCapabilities.codecs.forEach((lCodec) => {
            for (let i = 0; i < remoteCapabilities.codecs.length; i++) {
              const rCodec = remoteCapabilities.codecs[i];
              if (lCodec.name.toLowerCase() === rCodec.name.toLowerCase() &&
              lCodec.clockRate === rCodec.clockRate &&
              lCodec.numChannels === rCodec.numChannels) {
            // push rCodec so we reply with offerer payload type
                commonCapabilities.codecs.push(rCodec);

            // FIXME: also need to determine intersection between
            // .rtcpFeedback and .parameters
                break;
              }
            }
          });

          localCapabilities.headerExtensions.forEach((lHeaderExtension) => {
            for (let i = 0; i < remoteCapabilities.headerExtensions.length; i++) {
              const rHeaderExtension = remoteCapabilities.headerExtensions[i];
              if (lHeaderExtension.uri === rHeaderExtension.uri) {
                commonCapabilities.headerExtensions.push(rHeaderExtension);
                break;
              }
            }
          });

      // FIXME: fecMechanisms
          return commonCapabilities;
        };

    // Create ICE gatherer, ICE transport and DTLS transport.
    window.RTCPeerConnection.prototype._createIceAndDtlsTransports =
        function (mid, sdpMLineIndex) {
          const self = this;
          const iceGatherer = new RTCIceGatherer(self.iceOptions);
          const iceTransport = new RTCIceTransport(iceGatherer);
          iceGatherer.onlocalcandidate = function (evt) {
            const event = {};
            event.candidate = { sdpMid: mid, sdpMLineIndex };

            const cand = evt.candidate;
        // Edge emits an empty object for RTCIceCandidateComplete‥
            if (!cand || Object.keys(cand).length === 0) {
          // polyfill since RTCIceGatherer.state is not implemented in Edge 10547 yet.
              if (iceGatherer.state === undefined) {
                iceGatherer.state = 'completed';
              }

          // Emit a candidate with type endOfCandidates to make the samples work.
          // Edge requires addIceCandidate with this empty candidate to start checking.
          // The real solution is to signal end-of-candidates to the other side when
          // getting the null candidate but some apps (like the samples) don't do that.
              event.candidate.candidate =
              'candidate:1 1 udp 1 0.0.0.0 9 typ endOfCandidates';
            } else {
          // RTCIceCandidate doesn't have a component, needs to be added
              cand.component = iceTransport.component === 'RTCP' ? 2 : 1;
              event.candidate.candidate = SDPUtils.writeCandidate(cand);
            }

            const complete = self.transceivers.every(transceiver => transceiver.iceGatherer &&
              transceiver.iceGatherer.state === 'completed');
        // FIXME: update .localDescription with candidate and (potentially) end-of-candidates.
        //     To make this harder, the gatherer might emit candidates before localdescription
        //     is set. To make things worse, gather.getLocalCandidates still errors in
        //     Edge 10547 when no candidates have been gathered yet.

            if (self.onicecandidate !== null) {
          // Emit candidate if localDescription is set.
          // Also emits null candidate when all gatherers are complete.
              if (self.localDescription && self.localDescription.type === '') {
                self._localIceCandidatesBuffer.push(event);
                if (complete) {
                  self._localIceCandidatesBuffer.push({});
                }
              } else {
                self.onicecandidate(event);
                if (complete) {
                  self.onicecandidate({});
                }
              }
            }
          };
          iceTransport.onicestatechange = function () {
            self._updateConnectionState();
          };

          const dtlsTransport = new RTCDtlsTransport(iceTransport);
          dtlsTransport.ondtlsstatechange = function () {
            self._updateConnectionState();
          };
          dtlsTransport.onerror = function () {
        // onerror does not set state to failed by itself.
            dtlsTransport.state = 'failed';
            self._updateConnectionState();
          };

          return {
            iceGatherer,
            iceTransport,
            dtlsTransport,
          };
        };

    // Start the RTP Sender and Receiver for a transceiver.
    window.RTCPeerConnection.prototype._transceive = function (transceiver,
      send, recv) {
      const params = this._getCommonCapabilities(transceiver.localCapabilities,
          transceiver.remoteCapabilities);
      if (send && transceiver.rtpSender) {
        params.encodings = [{
          ssrc: transceiver.sendSsrc,
        }];
        params.rtcp = {
          cname: localCName,
          ssrc: transceiver.recvSsrc,
        };
        transceiver.rtpSender.send(params);
      }
      if (recv && transceiver.rtpReceiver) {
        params.encodings = [{
          ssrc: transceiver.recvSsrc,
        }];
        params.rtcp = {
          cname: transceiver.cname,
          ssrc: transceiver.sendSsrc,
        };
        transceiver.rtpReceiver.receive(params);
      }
    };

    window.RTCPeerConnection.prototype.setLocalDescription =
        function (description) {
          const self = this;
          if (description.type === 'offer') {
            if (!this._pendingOffer) {
            } else {
              this.transceivers = this._pendingOffer;
              delete this._pendingOffer;
            }
          } else if (description.type === 'answer') {
            const sections = SDPUtils.splitSections(self.remoteDescription.sdp);
            const sessionpart = sections.shift();
            sections.forEach((mediaSection, sdpMLineIndex) => {
              const transceiver = self.transceivers[sdpMLineIndex];
              const iceGatherer = transceiver.iceGatherer;
              const iceTransport = transceiver.iceTransport;
              const dtlsTransport = transceiver.dtlsTransport;
              const localCapabilities = transceiver.localCapabilities;
              const remoteCapabilities = transceiver.remoteCapabilities;
              const rejected = mediaSection.split('\n', 1)[0]
              .split(' ', 2)[1] === '0';

              if (!rejected) {
                const remoteIceParameters = SDPUtils.getIceParameters(mediaSection,
                sessionpart);
                iceTransport.start(iceGatherer, remoteIceParameters, 'controlled');

                const remoteDtlsParameters = SDPUtils.getDtlsParameters(mediaSection,
              sessionpart);
                dtlsTransport.start(remoteDtlsParameters);

            // Calculate intersection of capabilities.
                const params = self._getCommonCapabilities(localCapabilities,
                remoteCapabilities);

            // Start the RTCRtpSender. The RTCRtpReceiver for this transceiver
            // has already been started in setRemoteDescription.
                self._transceive(transceiver,
                params.codecs.length > 0,
                false);
              }
            });
          }

          this.localDescription = description;
          switch (description.type) {
            case 'offer':
              this._updateSignalingState('have-local-offer');
              break;
            case 'answer':
              this._updateSignalingState('stable');
              break;
            default:
              throw new TypeError(`unsupported type "${description.type}"`);
          }

      // If a success callback was provided, emit ICE candidates after it has been
      // executed. Otherwise, emit callback after the Promise is resolved.
          const hasCallback = arguments.length > 1 &&
        typeof arguments[1] === 'function';
          if (hasCallback) {
            const cb = arguments[1];
            window.setTimeout(() => {
              cb();
              self._emitBufferedCandidates();
            }, 0);
          }
          const p = Promise.resolve();
          p.then(() => {
            if (!hasCallback) {
              window.setTimeout(self._emitBufferedCandidates.bind(self), 0);
            }
          });
          return p;
        };

    window.RTCPeerConnection.prototype.setRemoteDescription =
        function (description) {
          const self = this;
          const stream = new MediaStream();
          const sections = SDPUtils.splitSections(description.sdp);
          const sessionpart = sections.shift();
          sections.forEach((mediaSection, sdpMLineIndex) => {
            const lines = SDPUtils.splitLines(mediaSection);
            const mline = lines[0].substr(2).split(' ');
            const kind = mline[0];
            const rejected = mline[1] === '0';
            const direction = SDPUtils.getDirection(mediaSection, sessionpart);

            let transceiver;
            let iceGatherer;
            let iceTransport;
            let dtlsTransport;
            let rtpSender;
            let rtpReceiver;
            let sendSsrc;
            let recvSsrc;
            let localCapabilities;

        // FIXME: ensure the mediaSection has rtcp-mux set.
            const remoteCapabilities = SDPUtils.parseRtpParameters(mediaSection);
            let remoteIceParameters;
            let remoteDtlsParameters;
            if (!rejected) {
              remoteIceParameters = SDPUtils.getIceParameters(mediaSection,
              sessionpart);
              remoteDtlsParameters = SDPUtils.getDtlsParameters(mediaSection,
              sessionpart);
            }
            const mid = SDPUtils.matchPrefix(mediaSection, 'a=mid:')[0].substr(6);

            let cname;
        // Gets the first SSRC. Note that with RTX there might be multiple SSRCs.
            const remoteSsrc = SDPUtils.matchPrefix(mediaSection, 'a=ssrc:')
            .map(line => SDPUtils.parseSsrcMedia(line))
            .filter(obj => obj.attribute === 'cname')[0];
            if (remoteSsrc) {
              recvSsrc = parseInt(remoteSsrc.ssrc, 10);
              cname = remoteSsrc.value;
            }

            if (description.type === 'offer') {
              const transports = self._createIceAndDtlsTransports(mid, sdpMLineIndex);

              localCapabilities = RTCRtpReceiver.getCapabilities(kind);
              sendSsrc = (2 * sdpMLineIndex + 2) * 1001;

              rtpReceiver = new RTCRtpReceiver(transports.dtlsTransport, kind);

          // FIXME: not correct when there are multiple streams but that is
          // not currently supported in this shim.
              stream.addTrack(rtpReceiver.track);

          // FIXME: look at direction.
              if (self.localStreams.length > 0 &&
              self.localStreams[0].getTracks().length >= sdpMLineIndex) {
            // FIXME: actually more complicated, needs to match types etc
                const localtrack = self.localStreams[0].getTracks()[sdpMLineIndex];
                rtpSender = new RTCRtpSender(localtrack, transports.dtlsTransport);
              }

              self.transceivers[sdpMLineIndex] = {
                iceGatherer: transports.iceGatherer,
                iceTransport: transports.iceTransport,
                dtlsTransport: transports.dtlsTransport,
                localCapabilities,
                remoteCapabilities,
                rtpSender,
                rtpReceiver,
                kind,
                mid,
                cname,
                sendSsrc,
                recvSsrc,
              };
          // Start the RTCRtpReceiver now. The RTPSender is started in setLocalDescription.
              self._transceive(self.transceivers[sdpMLineIndex],
              false,
              direction === 'sendrecv' || direction === 'sendonly');
            } else if (description.type === 'answer' && !rejected) {
              transceiver = self.transceivers[sdpMLineIndex];
              iceGatherer = transceiver.iceGatherer;
              iceTransport = transceiver.iceTransport;
              dtlsTransport = transceiver.dtlsTransport;
              rtpSender = transceiver.rtpSender;
              rtpReceiver = transceiver.rtpReceiver;
              sendSsrc = transceiver.sendSsrc;
          // recvSsrc = transceiver.recvSsrc;
              localCapabilities = transceiver.localCapabilities;

              self.transceivers[sdpMLineIndex].recvSsrc = recvSsrc;
              self.transceivers[sdpMLineIndex].remoteCapabilities =
              remoteCapabilities;
              self.transceivers[sdpMLineIndex].cname = cname;

              iceTransport.start(iceGatherer, remoteIceParameters, 'controlling');
              dtlsTransport.start(remoteDtlsParameters);

              self._transceive(transceiver,
              direction === 'sendrecv' || direction === 'recvonly',
              direction === 'sendrecv' || direction === 'sendonly');

              if (rtpReceiver &&
              (direction === 'sendrecv' || direction === 'sendonly')) {
                stream.addTrack(rtpReceiver.track);
              } else {
            // FIXME: actually the receiver should be created later.
                delete transceiver.rtpReceiver;
              }
            }
          });

          this.remoteDescription = description;
          switch (description.type) {
            case 'offer':
              this._updateSignalingState('have-remote-offer');
              break;
            case 'answer':
              this._updateSignalingState('stable');
              break;
            default:
              throw new TypeError(`unsupported type "${description.type}"`);
          }
          window.setTimeout(() => {
            if (self.onaddstream !== null && stream.getTracks().length) {
              self.remoteStreams.push(stream);
              window.setTimeout(() => {
                self.onaddstream({ stream });
              }, 0);
            }
          }, 0);
          if (arguments.length > 1 && typeof arguments[1] === 'function') {
            window.setTimeout(arguments[1], 0);
          }
          return Promise.resolve();
        };

    window.RTCPeerConnection.prototype.close = function () {
      this.transceivers.forEach((transceiver) => {
        /* not yet
        if (transceiver.iceGatherer) {
          transceiver.iceGatherer.close();
        }
        */
        if (transceiver.iceTransport) {
          transceiver.iceTransport.stop();
        }
        if (transceiver.dtlsTransport) {
          transceiver.dtlsTransport.stop();
        }
        if (transceiver.rtpSender) {
          transceiver.rtpSender.stop();
        }
        if (transceiver.rtpReceiver) {
          transceiver.rtpReceiver.stop();
        }
      });
      // FIXME: clean up tracks, local streams, remote streams, etc
      this._updateSignalingState('closed');
    };

    // Update the signaling state.
    window.RTCPeerConnection.prototype._updateSignalingState =
        function (newState) {
          this.signalingState = newState;
          if (this.onsignalingstatechange !== null) {
            this.onsignalingstatechange();
          }
        };

    // Determine whether to fire the negotiationneeded event.
    window.RTCPeerConnection.prototype._maybeFireNegotiationNeeded =
        function () {
      // Fire away (for now).
          if (this.onnegotiationneeded !== null) {
            this.onnegotiationneeded();
          }
        };

    // Update the connection state.
    window.RTCPeerConnection.prototype._updateConnectionState =
        function () {
          const self = this;
          let newState;
          const states = {
            new: 0,
            closed: 0,
            connecting: 0,
            checking: 0,
            connected: 0,
            completed: 0,
            failed: 0,
          };
          this.transceivers.forEach((transceiver) => {
            states[transceiver.iceTransport.state]++;
            states[transceiver.dtlsTransport.state]++;
          });
      // ICETransport.completed and connected are the same for this purpose.
          states.connected += states.completed;

          newState = 'new';
          if (states.failed > 0) {
            newState = 'failed';
          } else if (states.connecting > 0 || states.checking > 0) {
            newState = 'connecting';
          } else if (states.disconnected > 0) {
            newState = 'disconnected';
          } else if (states.new > 0) {
            newState = 'new';
          } else if (states.connecting > 0 || states.completed > 0) {
            newState = 'connected';
          }

          if (newState !== self.iceConnectionState) {
            self.iceConnectionState = newState;
            if (this.oniceconnectionstatechange !== null) {
              this.oniceconnectionstatechange();
            }
          }
        };

    window.RTCPeerConnection.prototype.createOffer = function () {
      const self = this;
      if (this._pendingOffer) {
        throw new Error('createOffer called while there is a pending offer.');
      }
      let offerOptions;
      if (arguments.length === 1 && typeof arguments[0] !== 'function') {
        offerOptions = arguments[0];
      } else if (arguments.length === 3) {
        offerOptions = arguments[2];
      }

      const tracks = [];
      let numAudioTracks = 0;
      let numVideoTracks = 0;
      // Default to sendrecv.
      if (this.localStreams.length) {
        numAudioTracks = this.localStreams[0].getAudioTracks().length;
        numVideoTracks = this.localStreams[0].getVideoTracks().length;
      }
      // Determine number of audio and video tracks we need to send/recv.
      if (offerOptions) {
        // Reject Chrome legacy constraints.
        if (offerOptions.mandatory || offerOptions.optional) {
          throw new TypeError(
              'Legacy mandatory/optional constraints not supported.');
        }
        if (offerOptions.offerToReceiveAudio !== undefined) {
          numAudioTracks = offerOptions.offerToReceiveAudio;
        }
        if (offerOptions.offerToReceiveVideo !== undefined) {
          numVideoTracks = offerOptions.offerToReceiveVideo;
        }
      }
      if (this.localStreams.length) {
        // Push local streams.
        this.localStreams[0].getTracks().forEach((track) => {
          tracks.push({
            kind: track.kind,
            track,
            wantReceive: track.kind === 'audio' ?
                numAudioTracks > 0 : numVideoTracks > 0,
          });
          if (track.kind === 'audio') {
            numAudioTracks--;
          } else if (track.kind === 'video') {
            numVideoTracks--;
          }
        });
      }
      // Create M-lines for recvonly streams.
      while (numAudioTracks > 0 || numVideoTracks > 0) {
        if (numAudioTracks > 0) {
          tracks.push({
            kind: 'audio',
            wantReceive: true,
          });
          numAudioTracks--;
        }
        if (numVideoTracks > 0) {
          tracks.push({
            kind: 'video',
            wantReceive: true,
          });
          numVideoTracks--;
        }
      }

      let sdp = SDPUtils.writeSessionBoilerplate();
      const transceivers = [];
      tracks.forEach((mline, sdpMLineIndex) => {
        // For each track, create an ice gatherer, ice transport, dtls transport,
        // potentially rtpsender and rtpreceiver.
        const track = mline.track;
        const kind = mline.kind;
        const mid = generateIdentifier();

        const transports = self._createIceAndDtlsTransports(mid, sdpMLineIndex);

        const localCapabilities = RTCRtpSender.getCapabilities(kind);
        let rtpSender;
        let rtpReceiver;

        // generate an ssrc now, to be used later in rtpSender.send
        const sendSsrc = (2 * sdpMLineIndex + 1) * 1001;
        if (track) {
          rtpSender = new RTCRtpSender(track, transports.dtlsTransport);
        }

        if (mline.wantReceive) {
          rtpReceiver = new RTCRtpReceiver(transports.dtlsTransport, kind);
        }

        transceivers[sdpMLineIndex] = {
          iceGatherer: transports.iceGatherer,
          iceTransport: transports.iceTransport,
          dtlsTransport: transports.dtlsTransport,
          localCapabilities,
          remoteCapabilities: null,
          rtpSender,
          rtpReceiver,
          kind,
          mid,
          sendSsrc,
          recvSsrc: null,
        };
        const transceiver = transceivers[sdpMLineIndex];
        sdp += SDPUtils.writeMediaSection(transceiver,
            transceiver.localCapabilities, 'offer', self.localStreams[0]);
      });

      this._pendingOffer = transceivers;
      const desc = new RTCSessionDescription({
        type: 'offer',
        sdp,
      });
      if (arguments.length && typeof arguments[0] === 'function') {
        window.setTimeout(arguments[0], 0, desc);
      }
      return Promise.resolve(desc);
    };

    window.RTCPeerConnection.prototype.createAnswer = function () {
      const self = this;
      let answerOptions;
      if (arguments.length === 1 && typeof arguments[0] !== 'function') {
        answerOptions = arguments[0];
      } else if (arguments.length === 3) {
        answerOptions = arguments[2];
      }

      let sdp = SDPUtils.writeSessionBoilerplate();
      this.transceivers.forEach((transceiver) => {
        // Calculate intersection of capabilities.
        const commonCapabilities = self._getCommonCapabilities(
            transceiver.localCapabilities,
            transceiver.remoteCapabilities);

        sdp += SDPUtils.writeMediaSection(transceiver, commonCapabilities,
            'answer', self.localStreams[0]);
      });

      const desc = new RTCSessionDescription({
        type: 'answer',
        sdp,
      });
      if (arguments.length && typeof arguments[0] === 'function') {
        window.setTimeout(arguments[0], 0, desc);
      }
      return Promise.resolve(desc);
    };

    window.RTCPeerConnection.prototype.addIceCandidate = function (candidate) {
      let mLineIndex = candidate.sdpMLineIndex;
      if (candidate.sdpMid) {
        for (let i = 0; i < this.transceivers.length; i++) {
          if (this.transceivers[i].mid === candidate.sdpMid) {
            mLineIndex = i;
            break;
          }
        }
      }
      const transceiver = this.transceivers[mLineIndex];
      if (transceiver) {
        let cand = Object.keys(candidate.candidate).length > 0 ?
            SDPUtils.parseCandidate(candidate.candidate) : {};
        // Ignore Chrome's invalid candidates since Edge does not like them.
        if (cand.protocol === 'tcp' && cand.port === 0) {
          return;
        }
        // Ignore RTCP candidates, we assume RTCP-MUX.
        if (cand.component !== '1') {
          return;
        }
        // A dirty hack to make samples work.
        if (cand.type === 'endOfCandidates') {
          cand = {};
        }
        transceiver.iceTransport.addRemoteCandidate(cand);
      }
      if (arguments.length > 1 && typeof arguments[1] === 'function') {
        window.setTimeout(arguments[1], 0);
      }
      return Promise.resolve();
    };

    window.RTCPeerConnection.prototype.getStats = function () {
      const promises = [];
      this.transceivers.forEach((transceiver) => {
        ['rtpSender', 'rtpReceiver', 'iceGatherer', 'iceTransport',
          'dtlsTransport'].forEach((method) => {
            if (transceiver[method]) {
              promises.push(transceiver[method].getStats());
            }
          });
      });
      const cb = arguments.length > 1 && typeof arguments[1] === 'function' &&
          arguments[1];
      return new Promise((resolve) => {
        const results = {};
        Promise.all(promises).then((res) => {
          res.forEach((result) => {
            Object.keys(result).forEach((id) => {
              results[id] = result[id];
            });
          });
          if (cb) {
            window.setTimeout(cb, 0, results);
          }
          resolve(results);
        });
      });
    };
  }
} else {
  webrtcUtils.log('Browser does not appear to be WebRTC-capable');
}

// Returns the result of getUserMedia as a Promise.
function requestUserMedia(constraints) {
  return new Promise((resolve, reject) => {
    getUserMedia(constraints, resolve, reject);
  });
}

const webrtcTesting = {};
try {
  Object.defineProperty(webrtcTesting, 'version', {
    set(version) {
      webrtcDetectedVersion = version;
    },
  });
} catch (e) {}

if (typeof module !== 'undefined') {
  let RTCPeerConnection;
  let RTCIceCandidate;
  var RTCSessionDescription;
  if (typeof window !== 'undefined') {
    RTCPeerConnection = window.RTCPeerConnection;
    RTCIceCandidate = window.RTCIceCandidate;
    RTCSessionDescription = window.RTCSessionDescription;
  }
  module.exports = {
    RTCPeerConnection,
    RTCIceCandidate,
    RTCSessionDescription,
    getUserMedia,
    attachMediaStream,
    reattachMediaStream,
    webrtcDetectedBrowser,
    webrtcDetectedVersion,
    webrtcMinimumVersion,
    webrtcTesting,
    webrtcUtils,
    // requestUserMedia: not exposed on purpose.
    // trace: not exposed on purpose.
  };
} else if ((typeof require === 'function') && (typeof define === 'function')) {
  // Expose objects and functions when RequireJS is doing the loading.
  define([], () => ({
    RTCPeerConnection: window.RTCPeerConnection,
    RTCIceCandidate: window.RTCIceCandidate,
    RTCSessionDescription: window.RTCSessionDescription,
    getUserMedia,
    attachMediaStream,
    reattachMediaStream,
    webrtcDetectedBrowser,
    webrtcDetectedVersion,
    webrtcMinimumVersion,
    webrtcTesting,
    webrtcUtils,
      // requestUserMedia: not exposed on purpose.
      // trace: not exposed on purpose.
  }));
}
