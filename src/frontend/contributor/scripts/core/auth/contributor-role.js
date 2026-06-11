export const CONTRIBUTOR_ROLE = 'contributor';

export function isContributorUser(user) {
  return String(user?.role || '').toLowerCase() === CONTRIBUTOR_ROLE;
}
