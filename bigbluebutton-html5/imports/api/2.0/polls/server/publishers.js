import { Meteor } from 'meteor/meteor';
import { check } from 'meteor/check';
import Polls from '/imports/api/2.0/polls';
import mapToAcl from '/imports/startup/mapToAcl';

function polls(credentials) {
  const { meetingId, requesterUserId, requesterToken } = credentials;

  check(meetingId, String);
  check(requesterUserId, String);
  check(requesterToken, String);

  const selector = {
    meetingId,
    users: requesterUserId,
  };

  return Polls.find(selector);
}

function publish(...args) {
  const boundPolls = polls.bind(this);
  return mapToAcl('subscriptions.polls', boundPolls)(args);
}

Meteor.publish('polls', publish);

