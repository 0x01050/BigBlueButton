import clearBreakouts from '../modifiers/clearBreakouts';
import { check } from 'meteor/check';

export default function breakoutClosed({ payload }) {
  const meetingId = payload.meetingId;

  check(meetingId, String);

  return clearBreakouts(meetingId);
}
