import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

const HOME_PAGE_NUM_OF_ROWS = 10;

export const fetchTenders = createAsyncThunk(
    'tenders/fetchTenders',
    async ({ pageNo = 1 } = {}) => {
        // ✅ numOfRows를 고정된 값으로 사용
        const response = await fetch(`http://localhost:8080/api/tenders?pageNo=${pageNo}&numOfRows=${HOME_PAGE_NUM_OF_ROWS}`);
        
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP error! status: ${response.status} - ${errorText}`);
        }
        const data = await response.json();
        console.log(data);
        return data;
    }
);

const tenderSlice = createSlice({
    name: 'tenders',
    initialState: {
        bids: [],
        status: 'idle', // 'idle' | 'loading' | 'succeeded' | 'failed'
        error: null,
        currentPage: 1,   // ✅ 현재 페이지 번호
        totalCount: 0,    // ✅ 전체 입찰 수
        numOfRows: HOME_PAGE_NUM_OF_ROWS,
    },
    reducers: {
        setCurrentPageRedux: (state, action) => {
            state.currentPage = action.payload;
        },
    },

    extraReducers: (builder) => {
        builder

            // fetchTenders
            .addCase(fetchTenders.pending, (state) => {
                state.status = 'loading';
                state.error = null;
            })
            .addCase(fetchTenders.fulfilled, (state, action) => {
                if (action.payload) {
                    state.bids = action.payload.tenders || [];
                    state.totalCount = action.payload.totalCount; // ✅ 전체 count는 백엔드에서 받은 값 그대로 사용
                    state.currentPage = action.payload.pageNo;
                    state.numOfRows = HOME_PAGE_NUM_OF_ROWS; // ✅ numOfRows는 고정값 유지
                } else {
                    state.bids = [];
                    state.totalCount = 0;
                    state.currentPage = 1;
                    state.numOfRows = HOME_PAGE_NUM_OF_ROWS;
                }
            })
            .addCase(fetchTenders.rejected, (state, action) => {
                state.status = 'failed';
                state.error = action.payload || action.error.message; // rejectWithValue로 전달된 에러 메시지
            });
    },
});

export const { setCurrentPageRedux } = tenderSlice.actions;
export default tenderSlice.reducer;