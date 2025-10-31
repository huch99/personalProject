import { configureStore } from '@reduxjs/toolkit';
import tendersReducer from '../features/tenders/tenderSlicce'; 

export const store = configureStore({

    reducer : {
        tenders : tendersReducer,
    },
});