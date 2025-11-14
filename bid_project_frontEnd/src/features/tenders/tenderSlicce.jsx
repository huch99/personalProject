import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

const HOME_PAGE_NUM_OF_ROWS = 10;

export const fetchTenders = createAsyncThunk(
    'tenders/fetchTenders',
    async ({ pageNo = 1, numOfRows = HOME_PAGE_NUM_OF_ROWS } = {}) => {
        const response = await fetch(`http://localhost:8080/api/tenders?pageNo=${pageNo}&numOfRows=${numOfRows}`);
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP error! status: ${response.status} - ${errorText}`);
        }
        const data = await response.json();
        return data;
    }
);

// ✅ 상세 검색 전용 thunk 생성
export const fetchSearchTenders = createAsyncThunk(
    'tenders/fetchSearchTenders',
    async (searchCriteria = {}) => {
        const params = new URLSearchParams();

        if (searchCriteria.cltrNm) params.append('cltrNm', searchCriteria.cltrNm);
        if (searchCriteria.dpslMtdCd) params.append('dpslMtdCd', searchCriteria.dpslMtdCd);
        if (searchCriteria.sido) params.append('sido', searchCriteria.sido);
        if (searchCriteria.sgk) params.append('sgk', searchCriteria.sgk);
        if (searchCriteria.emd) params.append('emd', searchCriteria.emd);
        if (searchCriteria.goodsPriceFrom) params.append('goodsPriceFrom', searchCriteria.goodsPriceFrom);
        if (searchCriteria.goodsPriceTo) params.append('goodsPriceTo', searchCriteria.goodsPriceTo);
        if (searchCriteria.pbctBegnDtm) params.append('pbctBegnDtm', `${searchCriteria.pbctBegnDtm} 00:00:00`);
        if (searchCriteria.pbctClsDtm) params.append('pbctClsDtm', `${searchCriteria.pbctClsDtm} 23:59:59`);
        if (searchCriteria.pbctBegnDtm) {
            const beginDtm = searchCriteria.pbctBegnDtm.includes(' ') ? searchCriteria.pbctBegnDtm : `${searchCriteria.pbctBegnDtm} 00:00:00`;
            params.append('pbctBegnDtm', beginDtm);
        }
        if (searchCriteria.pbctClsDtm) {
            const clsDtm = searchCriteria.pbctClsDtm.includes(' ') ? searchCriteria.pbctClsDtm : `${searchCriteria.pbctClsDtm} 23:59:59`;
            params.append('pbctClsDtm', clsDtm);
        }

        params.append('pageNo', searchCriteria.pageNo || '1');
        params.append('numOfRows', searchCriteria.numOfRows || '10');

        const url = `http://localhost:8080/api/tenders/search?${params.toString()}`;
        const response = await fetch(url);

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP error! status: ${response.status} - ${errorText}`);
        }
        const data = await response.json();
        return data;
    }
);

// ✅ 현재 사용자의 즐겨찾기 입찰 목록 ID를 가져오는 Thunk
export const fetchFavoriteTenderIds = createAsyncThunk(
    'tenders/fetchFavoriteTenderIds',
    async (_, { rejectWithValue }) => {
        try {
            const token = localStorage.getItem('accessToken');
            const response = await fetch('http://localhost:8080/api/favorites', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`HTTP error! status: ${response.status} - ${errorText}`);
            }

            return (await response.json()).map(tender => tender.cltrMnmtNo);
        } catch (error) {
            return rejectWithValue(error.response?.data || error.message);
        }
    }
);

// ✅ 즐겨찾기 상태를 토글하는 Thunk
export const toggleFavorite = createAsyncThunk(
    'tenders/toggleFavorite',
    async ({ cltrMnmtNo, isFavorite }, { rejectWithValue }) => {
        try {
            const token = localStorage.getItem('accessToken');
            const headers = { 'Authorization': `Bearer ${token}` };
            if (isFavorite) {
                const response = await fetch(`http://localhost:8080/api/favorites/${cltrMnmtNo}`, {
                    method: 'DELETE',
                    headers: headers
                });
                if (!response.ok) {
                    const errorText = await response.text();
                    throw new Error(`HTTP error! status: ${response.status} - ${errorText}`);
                }
                const data = await response.json();
                return { cltrMnmtNo, isFavorite: data.isFavorite };
            } else {
                const response = await fetch(`http://localhost:8080/api/favorites/${cltrMnmtNo}`, {
                    method: 'POST',
                    headers: headers
                });
                if (!response.ok) {
                    const errorText = await response.text();
                    throw new Error(`HTTP error! status: ${response.status} - ${errorText}`);
                }
                const data = await response.json();
                return { cltrMnmtNo, isFavorite: data.isFavorite };
            }
        } catch (error) {
            return rejectWithValue(error.response?.data || error.message);
        }
    }
);

// ✅ 단일 입찰 공고의 즐겨찾기 상태를 확인하는 Thunk (주로 상세 페이지에서 사용)
export const checkSingleFavoriteStatus = createAsyncThunk(
    'tenders/checkSingleFavoriteStatus',
    async (cltrMnmtNo, { rejectWithValue }) => {
        try {
            const token = localStorage.getItem('accessToken');
            const response = await fetch(`http://localhost:8080/api/favorites/check/${cltrMnmtNo}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`HTTP error! status: ${response.status} - ${errorText}`);
            }
            const data = await response.json();
            return { cltrMnmtNo, isFavorite: data.isFavorite };
        } catch (error) {
            return rejectWithValue(error.response?.data || error.message);
        }
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

        favoriteTenderIds: [], // 사용자가 즐겨찾기한 tenderId 목록 (string 또는 number)
        isFavoriteLoading: 'idle', // 'idle' | 'pending' | 'succeeded' | 'failed'
        isFavoriteError: null,
    },
    reducers: {
        setCurrentPageRedux: (state, action) => {
            state.currentPage = action.payload;
        },
        setNumOfRowsRedux: (state, action) => {
            state.numOfRows = action.payload;
        }
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
            })

            // ✅ fetchSearchTenders (상세 검색용)
            .addCase(fetchSearchTenders.pending, (state) => {
                state.status = 'loading';
                state.error = null;
            })
            .addCase(fetchSearchTenders.fulfilled, (state, action) => {
                state.status = 'succeeded';
                if (action.payload) {
                    state.bids = action.payload.tenders || [];
                    state.totalCount = action.payload.totalCount;
                    state.currentPage = action.payload.pageNo;
                    state.numOfRows = action.payload.numOfRows; // ✅ 상세 검색은 요청받은 numOfRows를 반영
                } else {
                    state.bids = [];
                    state.totalCount = 0;
                    state.currentPage = 1;
                    state.numOfRows = 10;
                }
            })
            .addCase(fetchSearchTenders.rejected, (state, action) => {
                state.status = 'failed';
                state.error = action.payload || action.error.message;
            })

            // ✅ fetchFavoriteTenderIds Thunk 처리
            .addCase(fetchFavoriteTenderIds.pending, (state) => {
                state.isFavoriteLoading = 'pending';
                state.isFavoriteError = null;
            })
            .addCase(fetchFavoriteTenderIds.fulfilled, (state, action) => {
                state.isFavoriteLoading = 'succeeded';
                // Set 대신 Array를 사용하여 직렬화 가능한 형태로 유지
                state.favoriteTenderIds = action.payload;
            })
            .addCase(fetchFavoriteTenderIds.rejected, (state, action) => {
                state.isFavoriteLoading = 'failed';
                state.isFavoriteError = action.payload || action.error.message;
            })

            // ✅ toggleFavorite Thunk 처리
            .addCase(toggleFavorite.fulfilled, (state, action) => {
                const { cltrMnmtNo, isFavorite } = action.payload;
                const index = state.favoriteTenderIds.indexOf(cltrMnmtNo);

                if (isFavorite && index === -1) {
                    state.favoriteTenderIds.push(cltrMnmtNo);
                } else if (!isFavorite && index !== -1) {
                    state.favoriteTenderIds.splice(index, 1);
                }
            })
            // ✅ checkSingleFavoriteStatus Thunk 처리
            // 상세 페이지에서 사용하며, 전역 favoriteTenderIds 상태도 업데이트할 수 있습니다.
            .addCase(checkSingleFavoriteStatus.fulfilled, (state, action) => {
                const { cltrMnmtNo, isFavorite } = action.payload;
                const index = state.favoriteTenderIds.indexOf(cltrMnmtNo);

                if (isFavorite && index === -1) {
                    state.favoriteTenderIds.push(cltrMnmtNo);
                } else if (!isFavorite && index !== -1) {
                    state.favoriteTenderIds.splice(index, 1);
                }
            });
    },
});

export const { setCurrentPageRedux, setNumOfRowsRedux } = tenderSlice.actions;
export default tenderSlice.reducer;