export async function logoutContributor(authRepository) {
  await authRepository.logout();
}
