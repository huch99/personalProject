import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useLocation, useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { setCurrentPageRedux, fetchSearchTenders, setNumOfRowsRedux, fetchFavoriteTenderIds, toggleFavorite } from '../features/tenders/tenderSlicce';
import { FaStar } from 'react-icons/fa';

const SearchPageContainer = styled.div`
  max-width: 1200px;
  margin: 40px auto;
  padding: 30px;
  background-color: #f9fbfd;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
`;

const SearchTitle = styled.h2`
  color: #2c3e50;
  margin-bottom: 30px;
  border-bottom: 3px solid #3498db;
  padding-bottom: 15px;
  font-size: 32px;
  text-align: center;
`;

const SearchForm = styled.form`
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 25px;
  padding: 25px;
  background-color: #ffffff;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  margin-bottom: 40px;
`;

const FormGroup = styled.div`
  display: flex;
  flex-direction: column;
`;

const Label = styled.label`
  font-weight: 600;
  margin-bottom: 8px;
  color: #34495e;
  font-size: 15px;
`;

const Input = styled.input`
  padding: 12px 14px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 15px;
  color: #495057;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;

  &:focus {
    border-color: #007bff;
    box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.25);
    outline: none;
  }
`;

const Select = styled.select`
  padding: 12px 14px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 15px;
  color: #495057;
  background-color: #fff;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='%236c757d' d='M7 10l5 5 5-5H7z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 16px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;

  &:focus {
    border-color: #007bff;
    box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.25);
    outline: none;
  }
`;

const RangeInputGroup = styled.div`
  display: flex;
  align-items: center;

  ${Input} {
    flex: 1;
    margin-right: 10px;
  }

  span {
    margin: 0 5px;
    color: #6c757d;
  }

  ${Input}:last-child {
    margin-right: 0;
  }
`;

const SearchButton = styled.button`
  grid-column: 1 / -1;
  padding: 15px 25px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s ease, box-shadow 0.2s ease;
  margin-top: 20px;

  &:hover {
    background-color: #0056b3;
    box-shadow: 0 4px 15px rgba(0, 123, 255, 0.3);
  }

  &:disabled {
    background-color: #cccccc;
    cursor: not-allowed;
  }
`;

const ResultList = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 25px;
  margin-top: 40px;
`;

const ResultItem = styled.div`
  background-color: #ffffff;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  position: relative; // ✅ FavoriteIcon 배치를 위해 추가

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
  }

  h3 {
    color: #34495e;
    font-size: 20px;
    margin-bottom: 15px;
    border-bottom: 1px solid #eee;
    padding-bottom: 10px;
  }

  p {
    color: #555;
    font-size: 14px;
    line-height: 1.5;
    margin-bottom: 5px;
  }

  strong {
    color: #333;
  }
`;

const StatusTag = styled.span`
  position: absolute;
  bottom: 15px;
  right: 15px;
  padding: 3px 8px;
  border-radius: 5px;
  font-size: 0.75rem;
  font-weight: bold;
  color: white;
  background-color: ${(props) => {
    if (props.$status === '입찰 진행중') return '#28a745';
    if (props.$status === '입찰 예정') return '#ffc107';
    if (props.$status === '입찰 마감') return '#dc3545';
    return '#6c757d';
  }};
`;

const LoadingMessage = styled.p`
  text-align: center;
  font-size: 18px;
  color: #6c757d;
  margin-top: 50px;
`;

const ErrorMessage = styled.p`
  text-align: center;
  font-size: 18px;
  color: #dc3545;
  margin-top: 50px;
`;

const PaginationContainer = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 40px;
  gap: 8px;
`;

const PageButton = styled.button`
  padding: 10px 15px;
  background-color: ${(props) => (props.active ? '#007bff' : '#f0f2f5')};
  color: ${(props) => (props.active ? 'white' : '#495057')};
  border: 1px solid #ced4da;
  border-radius: 5px;
  font-size: 15px;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease, border-color 0.2s ease;

  &:hover:not(:disabled) {
    background-color: ${(props) => (props.active ? '#0056b3' : '#e2e6ea')};
    border-color: ${(props) => (props.active ? '#0056b3' : '#dae0e5')};
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.6;
  }
`;

const FavoriteIcon = styled.div`
  position: absolute;
  top: 15px;
  right: 15px;
  cursor: pointer;
  color: ${props => props.isFavorite ? '#FFD700' : '#cccccc'}; // 노란색 or 회색
  font-size: 24px;
  transition: transform 0.2s ease;

  &:hover {
    transform: scale(1.1);
  }
`;

const DetailSearchPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const dispatch = useDispatch();

  // Redux state selector
  const bids = useSelector((state) => state.tenders.bids);
  const status = useSelector((state) => state.tenders.status);
  const error = useSelector((state) => state.tenders.error);
  const currentPage = useSelector((state) => state.tenders.currentPage);
  const totalCount = useSelector((state) => state.tenders.totalCount);
  const numOfRows = useSelector((state) => state.tenders.numOfRows);
  const favoriteTenderIds = useSelector((state) => state.tenders.favoriteTenderIds);

  // Local state for search parameters
  const [cltrNm, setCltrNm] = useState('');
  const [dpslMtdCd, setDpslMtdCd] = useState('');
  const [sido, setSido] = useState('');
  const [sgk, setSgk] = useState('');
  const [emd, setEmd] = useState('');
  const [minAppraisalPrice, setMinAppraisalPrice] = useState('');
  const [maxAppraisalPrice, setMaxAppraisalPrice] = useState('');
  const [pbctBegnDtm, setPbctBegnDtm] = useState('');
  const [pbctClsDtm, setPbctClsDtm] = useState('');
  const [currentBidStatus, setCurrentBidStatus] = useState('');

  // totalPages 계산 (Redux state를 기반으로)
  const totalPages = Math.ceil(totalCount / numOfRows);

  // 검색 실행 (Redux Thunk 디스패치)
  const executeSearch = useCallback((page, currentNumOfRows, currentSearchParams) => {
    page = page ?? 1;
    currentNumOfRows = currentNumOfRows ?? 10;

    const params = new URLSearchParams();

    if (currentSearchParams.cltrNm) params.append('cltrNm', currentSearchParams.cltrNm);
    if (currentSearchParams.dpslMtdCd) params.append('dpslMtdCd', currentSearchParams.dpslMtdCd);
    if (currentSearchParams.sido) params.append('sido', currentSearchParams.sido);
    if (currentSearchParams.sgk) params.append('sgk', currentSearchParams.sgk);
    if (currentSearchParams.emd) params.append('emd', currentSearchParams.emd);

    if (currentSearchParams.minAppraisalPrice) params.append('goodsPriceFrom', currentSearchParams.minAppraisalPrice);
    if (currentSearchParams.maxAppraisalPrice) params.append('goodsPriceTo', currentSearchParams.maxAppraisalPrice);

    if (currentSearchParams.pbctBegnDtm) params.append('pbctBegnDtm', pbctBegnDtm);
    if (currentSearchParams.pbctClsDtm) params.append('pbctClsDtm', pbctClsDtm);
    if (currentSearchParams.currentBidStatus) params.append('currentBidStatus', currentSearchParams.currentBidStatus);

    params.append('pageNo', page.toString());
    params.append('numOfRows', currentNumOfRows.toString());

    // URL 업데이트
    navigate(`/search?${params.toString()}`);

    dispatch(fetchSearchTenders({
      cltrNm: currentSearchParams.cltrNm,
      dpslMtdCd: currentSearchParams.dpslMtdCd,
      sido: currentSearchParams.sido,
      sgk: currentSearchParams.sgk,
      emd: currentSearchParams.emd,
      goodsPriceFrom: currentSearchParams.minAppraisalPrice,
      goodsPriceTo: currentSearchParams.maxAppraisalPrice,
      pbctBegnDtm: currentSearchParams.pbctBegnDtm,
      pbctClsDtm: currentSearchParams.pbctClsDtm,
      currentBidStatus: currentSearchParams.currentBidStatus,
      pageNo: page.toString(),
      numOfRows: currentNumOfRows.toString(),
    }));

  }, [navigate, dispatch]);

  // URL 쿼리 파라미터 변경 감지 및 상태 동기화
  useEffect(() => {
    const currentParams = new URLSearchParams(location.search);

    // 로컬 상태(useState) 업데이트
    setCltrNm(currentParams.get('cltrNm') || '');
    setDpslMtdCd(currentParams.get('dpslMtdCd') || '');
    setSido(currentParams.get('sido') || '');
    setSgk(currentParams.get('sgk') || '');
    setEmd(currentParams.get('emd') || '');
    setMinAppraisalPrice(currentParams.get('goodsPriceFrom') || '');
    setMaxAppraisalPrice(currentParams.get('goodsPriceTo') || '');
    setPbctBegnDtm(currentParams.get('pbctBegnDtm') || '');
    setPbctClsDtm(currentParams.get('pbctClsDtm') || '');
    setCurrentBidStatus(currentParams.get('currentBidStatus') || '');

    const urlPage = parseInt(currentParams.get('pageNo') || '1', 10);
    const urlNumOfRows = parseInt(currentParams.get('numOfRows') || '10', 10);

    // Redux 상태와 URL 파라미터 동기화
    if (urlNumOfRows !== numOfRows) {
      dispatch(setNumOfRowsRedux(urlNumOfRows));
    }

    if (urlPage !== currentPage) {
      dispatch(setCurrentPageRedux(urlPage));
    }


    const currentSearchParams = {
      cltrNm: currentParams.get('cltrNm') || '',
      dpslMtdCd: currentParams.get('dpslMtdCd') || '',
      sido: currentParams.get('sido') || '',
      sgk: currentParams.get('sgk') || '',
      emd: currentParams.get('emd') || '',
      minAppraisalPrice: currentParams.get('goodsPriceFrom') || '',
      maxAppraisalPrice: currentParams.get('goodsPriceTo') || '',
      pbctBegnDtm: currentParams.get('pbctBegnDtm') || '',
      pbctClsDtm: currentParams.get('pbctClsDtm') || '',
      currentBidStatus: currentParams.get('currentBidStatus') || '',
    };

    if (urlPage !== currentPage || urlNumOfRows !== numOfRows || (location.search && currentParams.toString() !== location.search.substring(1))) {
      executeSearch(urlPage, urlNumOfRows, currentSearchParams);
    } else if (!location.search) {
      executeSearch(urlPage, urlNumOfRows, currentSearchParams);
    }

    dispatch(fetchFavoriteTenderIds());
  }, [location.search, dispatch, executeSearch, currentPage, numOfRows]);

  // 검색 기능
  const handleSearch = useCallback((e) => {
    e.preventDefault();
    const currentSearchParams = {
      cltrNm, dpslMtdCd, sido, sgk, emd,
      minAppraisalPrice, maxAppraisalPrice,
      pbctBegnDtm, pbctClsDtm, currentBidStatus,
    };

    executeSearch(1, numOfRows, currentSearchParams);

  }, [cltrNm, dpslMtdCd, sido, sgk, emd, minAppraisalPrice, maxAppraisalPrice,
    pbctBegnDtm, pbctClsDtm, numOfRows, currentBidStatus, dispatch, executeSearch]);

  // 결과 표기 개수
  const handleItemsPerPageChange = useCallback((e) => {
    const newNumOfRows = Number(e.target.value);

    dispatch(setNumOfRowsRedux(newNumOfRows));

    const currentSearchParams = {
      cltrNm, dpslMtdCd, sido, sgk, emd,
      minAppraisalPrice, maxAppraisalPrice,
      pbctBegnDtm, pbctClsDtm,
      currentBidStatus,
    };

    executeSearch(1, newNumOfRows, currentSearchParams);

  }, [cltrNm, dpslMtdCd, sido, sgk, emd, minAppraisalPrice, maxAppraisalPrice,
    pbctBegnDtm, pbctClsDtm, currentBidStatus, dispatch, executeSearch]);

  // 페이징 기능
  const handlePageChange = useCallback((page) => {
    if (page < 1 || page > totalPages) return;
    dispatch(setCurrentPageRedux(page));

    const currentSearchParams = {
      cltrNm, dpslMtdCd, sido, sgk, emd,
      minAppraisalPrice, maxAppraisalPrice,
      pbctBegnDtm, pbctClsDtm,
      currentBidStatus,
    };

    executeSearch(page, numOfRows, currentSearchParams);

  }, [cltrNm, dpslMtdCd, sido, sgk, emd, minAppraisalPrice, maxAppraisalPrice,
    pbctBegnDtm, pbctClsDtm, currentBidStatus, dispatch, executeSearch, totalPages, numOfRows]);


  // ✅ 즐겨찾기 토글 핸들러
  const handleToggleFavorite = useCallback((e, cltrMnmtNo, currentIsFavorite) => {
    e.stopPropagation(); // ✅ 이벤트 버블링 방지 (아이템 클릭 -> 상세 페이지 이동 막기)
    dispatch(toggleFavorite({ cltrMnmtNo, isFavorite: currentIsFavorite }));
  }, [dispatch]);

  let content;
  const loading = status === 'loading';

  if (loading) {
    content = <LoadingMessage>검색 결과 로딩 중...</LoadingMessage>;
  } else if (status === 'succeeded') {
    if (bids && bids.length > 0) {
      content = (
        <ResultList>
          {bids.map((item) => (
            <ResultItem key={item.cltrMnmtNo}
              onClick={() => navigate(`/tenders/${item.cltrMnmtNo}`)}
              style={{ cursor: 'pointer' }}
            >
              <h3>{item.tenderTitle}</h3>

              {/* ✅ 상태 태그 표시 */}
            {item.status && <StatusTag $status={item.status}>{item.status}</StatusTag>}

              <p><strong>처분방식:</strong> {item.organization}</p>
              <p><strong>공고번호:</strong> {item.pbctNo}</p>
              <p><strong>물건관리번호:</strong> {item.cltrMnmtNo}</p>
              <p><strong>입찰마감일:</strong> {item.deadline ? new Date(item.deadline).toLocaleString() : 'N/A'}</p>

              {/* ✅ '입찰 진행중'일 때만 추가 정보 표시 */}
              {item.status === '입찰 진행중' && (
                <>
                  <p><strong>현재 최저 입찰가:</strong> {item.minBidPrice ? item.minBidPrice.toLocaleString() + '원' : '없음'}</p>
                  <p><strong>최초 감정가:</strong> {item.initialOpenPriceFrom && item.initialOpenPriceTo ?
                    `${item.initialOpenPriceFrom.toLocaleString()}원 ~ ${item.initialOpenPriceTo.toLocaleString()}원` : '정보 없음'}</p>
                </>
              )}

              {/* ✅ 즐겨찾기 아이콘 */}
              <FavoriteIcon
                isFavorite={favoriteTenderIds.includes(item.cltrMnmtNo)} // ID가 목록에 있으면 즐겨찾기 됨
                onClick={(e) => handleToggleFavorite(e, item.cltrMnmtNo, favoriteTenderIds.includes(item.cltrMnmtNo))}
              >
                <FaStar />
              </FavoriteIcon>
            </ResultItem>
          ))}
        </ResultList>
      );
    } else {
      content = <p>검색 결과가 없습니다.</p>;
    }
  } else if (status === 'failed') {
    content = <ErrorMessage>{error}</ErrorMessage>;
  }

  const pageNumbers = [];
  const maxPageButtons = 10;
  let startPage = Math.max(1, currentPage - Math.floor(maxPageButtons / 2));
  let endPage = Math.min(totalPages, startPage + maxPageButtons - 1);
  if (endPage - startPage + 1 < maxPageButtons) {
    startPage = Math.max(1, endPage - maxPageButtons + 1);
  }
  for (let i = startPage; i <= endPage; i++) {
    pageNumbers.push(i);
  }

  return (
    <SearchPageContainer>
      <SearchTitle>입찰 상세 검색</SearchTitle>

      <SearchForm onSubmit={handleSearch}>
        <FormGroup>
          <Label htmlFor="cltrNm">물건명</Label>
          <Input
            id="cltrNm"
            type="text"
            value={cltrNm}
            onChange={(e) => setCltrNm(e.target.value)}
            placeholder="물건명을 입력하세요."
          />
        </FormGroup>

        <FormGroup>
          <Label htmlFor="dpslMtdCd">처분방식</Label>
          <Select
            id="dpslMtdCd"
            value={dpslMtdCd}
            onChange={(e) => setDpslMtdCd(e.target.value)}
          >
            <option value="">전체</option>
            <option value="경쟁입찰">경쟁입찰</option>
            <option value="수의계약">수의계약</option>
          </Select>
        </FormGroup>

        <FormGroup>
          <Label htmlFor="sido">물건소재지 (시/도)</Label>
          <Input
            id="sido"
            type="text"
            value={sido}
            onChange={(e) => setSido(e.target.value)}
            placeholder="예: 서울"
          />
        </FormGroup>

        <FormGroup>
          <Label htmlFor="sgk">물건소재지 (시군구)</Label>
          <Input
            id="sgk"
            type="text"
            value={sgk}
            onChange={(e) => setSgk(e.target.value)}
            placeholder="예: 강남구"
          />
        </FormGroup>

        <FormGroup>
          <Label htmlFor="emd">물건소재지 (읍면동)</Label>
          <Input
            id="emd"
            type="text"
            value={emd}
            onChange={(e) => setEmd(e.target.value)}
            placeholder="예: 역삼동"
          />
        </FormGroup>

        <FormGroup style={{gridRow: 4}}>
          <Label>감정가 (원)</Label>
          <RangeInputGroup>
            <Input
              type="number"
              value={minAppraisalPrice}
              onChange={(e) => setMinAppraisalPrice(e.target.value)}
              placeholder="최소"
            />
            <span>~</span>
            <Input
              type="number"
              value={maxAppraisalPrice}
              onChange={(e) => setMaxAppraisalPrice(e.target.value)}
              placeholder="최대"
            />
          </RangeInputGroup>
        </FormGroup>

        <FormGroup>
            <Label htmlFor="currentBidStatus">입찰 상태</Label>
            <Select
                id="currentBidStatus"
                name="currentBidStatus"
                value={currentBidStatus}
                onChange={(e) => setCurrentBidStatus(e.target.value)}
            >
                <option value="입찰 진행중">입찰 진행중</option>
                <option value="입찰 예정">입찰 예정</option>
                <option value="입찰 마감">입찰 마감</option>
            </Select>
        </FormGroup>

        <FormGroup style={{gridRow: 5}}>
          <Label>입찰일자</Label>
          <RangeInputGroup>
            <Input
              type="date"
              value={pbctBegnDtm}
              onChange={(e) => setPbctBegnDtm(e.target.value)}
            />
            <span>~</span>
            <Input
              type="date"
              value={pbctClsDtm}
              onChange={(e) => setPbctClsDtm(e.target.value)}
            />
          </RangeInputGroup>
        </FormGroup>

        

        <FormGroup style={{gridRow: 5}}>
          <Label htmlFor="itemsPerPage">표시 개수</Label>
          <Select
            id="itemsPerPage"
            value={numOfRows}
            onChange={handleItemsPerPageChange}
          >
            <option value="10">10개</option>
            <option value="20">20개</option>
            <option value="50">50개</option>
            <option value="100">100개</option>
          </Select>
        </FormGroup>

        <SearchButton type="submit" disabled={loading} style={{gridRow: 6}}>
          {loading ? '검색 중...' : '검색'}
        </SearchButton>
      </SearchForm>

      {content}

      {!loading && !error && totalPages > 1 && (
        <PaginationContainer>
          <PageButton onClick={() => handlePageChange(1)} disabled={currentPage === 1}>
            {'<<'}
          </PageButton>
          <PageButton onClick={() => handlePageChange(currentPage - 1)} disabled={currentPage === 1}>
            {'<'}
          </PageButton>
          {pageNumbers.map((page) => (
            <PageButton key={page} onClick={() => handlePageChange(page)} active={page === currentPage}>
              {page}
            </PageButton>
          ))}
          <PageButton onClick={() => handlePageChange(currentPage + 1)} disabled={currentPage === totalPages}>
            {'>'}
          </PageButton>
          <PageButton onClick={() => handlePageChange(totalPages)} disabled={currentPage === totalPages}>
            {'>>'}
          </PageButton>
        </PaginationContainer>
      )}
    </SearchPageContainer>
  );
};

export default DetailSearchPage;