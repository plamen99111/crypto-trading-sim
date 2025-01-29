import { toast, Zoom } from 'react-toastify';

const toastOptions = {
    position: 'bottom-center',
    transition: Zoom,
    autoClose: 3000,
    closeOnClick: true,
    theme: "light",
};

const toastUtils = {
    showSuccessToast: (message) => {
        toast.success(message, toastOptions);
    },

    showErrorToast: (message) => {
        toast.error(message, toastOptions);
    },

    showInfoToast: (message) => {
        toast.info(message, toastOptions);
    },

    showLoadingToast: (message) => {
        return toast.loading(message, {
            position: 'bottom-center',
            transition: Zoom,
        });
    },

    updateLoadingToast: (toastId, message, type = 'success') => {
        toast.update(toastId, {
            render: message,
            type,
            transition: Zoom,
            isLoading: false,
            autoClose: 3000,
            closeOnClick: true,
            theme: "light",
        });
    }
};

export default toastUtils;