/**
 * Initializes the signup page
 */

const initSignupPage = () => {
  // references 
  const displayNameInput = document.getElementById('display-name');
  const emailInput = document.getElementById('email');
  const passwordInput = document.getElementById('password');
  const confirmInput = document.getElementById('confirm-password');
  const tosCheckbox = document.getElementById('tos');
  const submitBtn = document.getElementById('submit-btn');
  const errorMsg = document.getElementById('error-msg');
 
  // helper functions
  const showError = (message) => {
    errorMsg.textContent = escapeHtml(message);
    errorMsg.classList.remove('hidden');
  };
 
  const hideError = () => {
    errorMsg.textContent = '';
    errorMsg.classList.add('hidden');
  };
 
  const markInvalid = (input) => {
    input.classList.add('border-red-400', 'ring-2', 'ring-red-200');
    input.classList.remove('border-slate-200');
  };
 
  const markValid = (input) => {
    input.classList.remove('border-red-400', 'ring-2', 'ring-red-200');
    input.classList.add('border-slate-200');
  };
 
  const setLoading = (isLoading) => {
    submitBtn.disabled = isLoading;
    submitBtn.textContent = isLoading ? 'Creating account…' : 'Get Started';
  };
 
  const fieldMap = {
    'display-name': displayNameInput,
    'email': emailInput,
    'password': passwordInput,
    'confirm-password': confirmInput,
  };
 
  const highlightFailedField = (message) => {

    Object.values(fieldMap).forEach(markValid);
 
    if (message.toLowerCase().includes('display name')) {
      markInvalid(displayNameInput);
      displayNameInput.focus();
    } else if (message.toLowerCase().includes('email')) {
      markInvalid(emailInput);
      emailInput.focus();
    } else if (message.toLowerCase().includes('passwords do not match')) {
      markInvalid(confirmInput);
      confirmInput.focus();
    } else if (message.toLowerCase().includes('password')) {
      markInvalid(passwordInput);
      passwordInput.focus();
    }
  };
 
  Object.values(fieldMap).forEach((input) => {
    input.addEventListener('input', () => markValid(input));
  });
 
  // confirm password mismatch hint
  confirmInput.addEventListener('input', () => {
    if (confirmInput.value && confirmInput.value !== passwordInput.value) {
      markInvalid(confirmInput);
    } else {
      markValid(confirmInput);
    }
  });
 
  // submit handler
  submitBtn.addEventListener('click', async () => {
    hideError();
 
    if (!tosCheckbox.checked) {
      showError('You must agree to the Terms of Service and Privacy Policy.');
      return;
    }
 
    const input = {
      displayName: displayNameInput.value,
      email: emailInput.value,
      password: passwordInput.value,
      confirmPassword: confirmInput.value,
    };
 
    setLoading(true);
 
    const result = await registerContributorUseCase(input);
 
    if (!result.success) {
      setLoading(false);
      highlightFailedField(result.message);
      showError(result.message);
      return;
    }
 
    // success: redirect to verify page 
    window.location.href = 'verify.html';
  });
};