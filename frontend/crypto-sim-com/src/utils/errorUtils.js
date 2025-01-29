const handleNetworkError = (error, t) => {
  const NETWORK_ERROR_MESSAGE = 'Unable to connect to server. Please try again later.';

  if (error instanceof TypeError || error.message === 'Network Error' || error.message.includes('Failed to fetch')) {
    return NETWORK_ERROR_MESSAGE;
  }
  return null;
};

const handleErrorMessage = (error, customErrorMessage, t) => {
  let errorMessage = handleNetworkError(error, t);

  if (!errorMessage) {
    errorMessage = customErrorMessage;
  }

  return errorMessage;
};

export default {
  handleErrorMessage
};