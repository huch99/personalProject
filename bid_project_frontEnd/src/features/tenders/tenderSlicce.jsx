import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

export const fetchTenders = createAsyncThunk(
    'tenders/fetchTenders',
    async (_, { rejectWithValue }) => {
        try {
            const response = await fetch('http://localhost:8080/api/tenders');

            if (!response.ok) {
                const errorDetail = await response.json().catch(() => ({
                    message: `서버 오류 발생 : ${response.status}`,
                    statusCode: response.status
                }));

                return rejectWithValue(errorDetail.message  || `HTTP error! status : ${response.status}`);
            }

            const data = await response.json();
            console.log(data);
            return data;
            
        } catch (error) {
            console.error("공공 API 호출 중 오류 발생:", error);
            return rejectWithValue(error.message || "공공 입찰 정보를 불러오는데 실패했습니다. (네트워크 또는 CORS)");
        }
    }
);

const tenderSlice = createSlice({
    name: 'tenders',
    initialState: {
        bids: [],
        status: 'idle', // 'idle' | 'loading' | 'succeeded' | 'failed'
        error: null,
    },
    reducers: {

    },

    extraReducers: (builder) => {
        builder

            // fetchTenders
            .addCase(fetchTenders.pending, (state) => {
                state.status = 'loading';
                state.error = null;
            })
            .addCase(fetchTenders.fulfilled, (state, action) => {
                state.status = 'succeeded';
                state.bids = action.payload; // API로부터 받은 데이터를 bids에 저장
            })
            .addCase(fetchTenders.rejected, (state, action) => {
                state.status = 'failed';
                state.error = action.payload || action.error.message; // rejectWithValue로 전달된 에러 메시지
            });
    },
});

export default tenderSlice.reducer;