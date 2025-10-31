import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

// const PUBLIC_API_BASE_URL = "https://openapi.onbid.co.kr/openapi/services";
// const PUBLIC_API_KEY = "ac16b1820a4f5cb666b16d881e2ca7b2fba30d16306184e5609d73a935cb5442";
export const fetchTenders = createAsyncThunk(
    'tenders/fetchTenders',
    async (_, { rejectWithValue }) => {
        try {
            const response = await fetch('http://localhost:8080/api/tenders');
            if (!response.ok) {
                const errorData = await response.json();
                return rejectWithValue(errorData.message || `HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
            return data;

            // const params = new URLSearchParams({
            //     serviceKey: PUBLIC_API_KEY,
            //     pageNo: 1,
            //     numOfRows: 10,
            // });

            // const response = await fetch(`${PUBLIC_API_BASE_URL}?${params.toString()}`);

            // if (!response.ok) {
            //     const errorText = await response.text();
            //     return rejectWithValue(`API 호출 실패 : ${response.status} ${response.statusText} - ${errorText}`);
            // }

            // const rawApiData = await response.json();

            // const publicTenders = rawApiData.response.body.items;

            // const transformedBids = publicTenders.map(item => ({
            //     id: item.bidNo,
            //     title: item.bidNm,
            //     organization: item.orgNm, // 발주기관명 필드 (예시)
            //     bidNumber: item.bidNo, // 입찰번호 필드 (예시)
            //     announcementDate: item.ntceDt, // 공고일 필드 (예시)
            //     deadline: item.clsgDt,
            // }));

            // return transformedBids;

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
    reducer: {

    },

    extraReducer: (builder) => {
        builder

            // fetchTenders
            .addCase(fetchTenders.pending, (state, action) => {
                state.status = 'loading';
            })
            .addCase(fetchTenders.fulfilled, (state, action) => {
                state.status = 'succeeded';
                state.bids = action.payload; // API로부터 받은 데이터를 bids에 저장
            })
            .addCase(fetchTenders.rejected, (state, action) => {
                state.status = 'failed';
                state.error = action.payload; // rejectWithValue로 전달된 에러 메시지
            });
    },
});

export default tenderSlice.reducer;