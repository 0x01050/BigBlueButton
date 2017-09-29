import { check } from 'meteor/check';
import changeCurrentPresentation from '../modifiers/changeCurrentPresentation';

export default function presentationCurrentChange({ body }, meetingId) {
  check(body, Object);

  const { presentationId } = body;

  check(meetingId, String);
  check(presentationId, String);

  return changeCurrentPresentation(meetingId, presentationId);
}
