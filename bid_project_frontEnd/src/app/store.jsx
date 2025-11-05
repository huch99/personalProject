import { configureStore } from '@reduxjs/toolkit';
import tendersReducer from '../features/tenders/tenderSlicce'; 
import loginReducer from '../features/login_signup/loginSlice';

export const store = configureStore({

    reducer : {
        tenders : tendersReducer,
        login : loginReducer,
    },
});