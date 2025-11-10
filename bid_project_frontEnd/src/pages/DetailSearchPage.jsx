import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';

const SearchPageContainer = styled.div`
  max-width: 1200px;
  margin: 40px auto;
  padding: 25px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
`;

const SearchTitle = styled.h2`
  color: #333;
  margin-bottom: 25px;
  border-bottom: 2px solid #eee;
  padding-bottom: 10px;
  font-size: 28px;
  text-align: center;
`;

const SearchForm = styled.form`
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background-color: #f9f9f9;
`;

const FormGroup = styled.div`
  display: flex;
  flex-direction: column;
`;

const Label = styled.label`
  margin-bottom: 8px;
  font-weight: bold;
  color: #555;
`;

const Input = styled.input`
  padding: 10px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 16px;

  &:focus {
    outline: none;
    border-color: #007bff;
    box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
  }
`;

const Select = styled.select`
  padding: 10px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 16px;
  background-color: white;

  &:focus {
    outline: none;
    border-color: #007bff;
    box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
  }
`;

const RangeInputGroup = styled.div`
  display: flex;
  align-items: center;
  gap: 10px;

  ${Input} {
    flex: 1;
  }
`;

const SearchButton = styled.button`
  grid-column: 1 / -1; /* 폼 전체 너비 사용 */
  background-color: #007bff;
  color: white;
  padding: 12px 25px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 18px;
  transition: background-color 0.2s ease;
  margin-top: 20px;

  &:hover {
    background-color: #0056b3;
  }

  &:disabled {
    background-color: #cccccc;
    cursor: not-allowed;
  }
`;

const ResultList = styled.div`
  margin-top: 30px;
`;

const ResultItem = styled.div`
  background-color: #f0f8ff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 15px 20px;
  margin-bottom: 15px;

  h3 {
    color: #333;
    margin-bottom: 5px;
  }

  p {
    color: #666;
    font-size: 14px;
    margin-bottom: 3px;
  }
`;

const ErrorMessage = styled.p`
  color: red;
  text-align: center;
  font-size: 16px;
  margin-top: 20px;
`;

const LoadingMessage = styled.p`
  text-align: center;
  font-size: 16px;
  color: #555;
  margin-top: 20px;
`;

const PaginationContainer = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 30px;
  gap: 5px;
`;

const PageButton = styled.button`
  background-color: ${(props) => (props.active ? '#007bff' : '#f8f9fa')};
  color: ${(props) => (props.active ? 'white' : '#495057')};
  border: 1px solid #dee2e6;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;

  &:hover:not(:disabled) {
    background-color: ${(props) => (props.active ? '#0056b3' : '#e2e6ea')};
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.6;
  }
`;

const DetailSearchPage = () => {
    const navigate = useNavigate();

    // 검색 조건 상태
    const [searchTerm, setSearchTerm] = useState(''); // 물건명 (CLTR_NM)
    const [dpslMtdCd, setDpslMtdCd] = useState(''); // 처분방식코드 (DPSL_MTD_CD)
    const [sido, setSido] = useState(''); // 물건소재지 (시도)
    const [sgk, setSgk] = useState(''); // 물건소재지 (시군구)
    const [emd, setEmd] = useState(''); // 물건소재지 (읍면동)
    const [minAppraisalPrice, setMinAppraisalPrice] = useState(''); // 감정가 하한
    const [maxAppraisalPrice, setMaxAppraisalPrice] = useState(''); // 감정가 상한
    const [minBidPrice, setMinBidPrice] = useState(''); // 최저입찰가 하한
    const [maxBidPrice, setMaxBidPrice] = useState(''); // 최저입찰가 상한
    const [startDate, setStartDate] = useState(''); // 입찰일자 From
    const [endDate, setEndDate] = useState(''); // 입찰일자 To

    // 결과 및 로딩 상태
    const [searchResults, setSearchResults] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // ✅ 초기 렌더링 시 검색을 방지하기 위한 useRef (필요시 사용)
    const isInitialMount = useRef(true);

    // 페이징 관련 상태
    const [currentPage, setCurrentPage] = useState(1);
    const [totalItems, setTotalItems] = useState(0);
    const [itemsPerPage, setItemsPerPage] = useState(10);

    useEffect(() => {
        fetchSearchResults(currentPage, itemsPerPage);
    }, [currentPage, itemsPerPage]); // currentPage 또는 itemsPerPage 변경 시 재검색

    // ✅ fetchSearchResults 함수가 검색 조건을 인자로 받도록 변경
    const fetchSearchResults = async (page, rows) => { // ✅ 인자 목록 간소화
    setLoading(true);
    setError(null);
    
    try {
      const queryParams = new URLSearchParams();
      // ✅ 현재 컴포넌트의 useState 상태들을 사용
      if (searchTerm) queryParams.append('cltrNm', searchTerm);
      if (dpslMtdCd) queryParams.append('dpslMtdCd', dpslMtdCd);
      if (sido) queryParams.append('sido', sido);
      if (sgk) queryParams.append('sgk', sgk);
      if (emd) queryParams.append('emd', emd);
      if (minAppraisalPrice) queryParams.append('goodsPriceFrom', minAppraisalPrice);
      if (maxAppraisalPrice) queryParams.append('goodsPriceTo', maxAppraisalPrice);
      if (minBidPrice) queryParams.append('openPriceFrom', minBidPrice);
      if (maxBidPrice) queryParams.append('openPriceTo', maxBidPrice);
      if (startDate) queryParams.append('pbctBegnDtm', startDate.replace(/-/g, ''));
      if (endDate) queryParams.append('pbctClsDtm', endDate.replace(/-/g, ''));
      
      queryParams.append('pageNo', page);
      queryParams.append('numOfRows', rows);


      const response = await fetch(`http://localhost:8080/api/tenders/search?${queryParams.toString()}`);


      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP error! status: ${response.status} - ${errorText}`);
      }

      const pagedResponse = await response.json(); 
      setSearchResults(pagedResponse.tenders || []); 
      setTotalItems(pagedResponse.totalCount || 0);
      setCurrentPage(pagedResponse.pageNo || 1);
      setItemsPerPage(pagedResponse.numOfRows || 10);

    } catch (err) {
      setError(err.message || '검색 중 오류가 발생했습니다.');
      console.error("Failed to fetch detailed search results:", err);
      setSearchResults([]); 
      setTotalItems(0);     
    } finally {
      setLoading(false);
    }
  };

    // ✅ useEffect: currentPage 또는 itemsPerPage 변경 시 자동으로 검색
    useEffect(() => {
    // 최초 렌더링 시에는 검색하지 않고, 페이지네이션 변경 시에만 검색
    if (currentPage !== 1 || totalItems > 0) { // totalItems > 0 조건은 검색결과가 있을 때만 페이지 이동으로 api 호출하게
        fetchSearchResults(currentPage, itemsPerPage);
    } else { // 페이지가 1이거나 검색 전이라면 초기 검색 (빈 값으로 한번 호출)
        fetchSearchResults(1, itemsPerPage);
    }
    // 이펙트의 의존성 배열에서 검색 조건 상태값들을 모두 제거
  }, [currentPage, itemsPerPage]); // ✅ 의존성 배열 간소화

  // ✅ handleSubmit은 검색 버튼 클릭 시 currentPage를 1로 재설정하고, 즉시 fetchSearchResults 호출
  const handleSearch = (e) => {
    e.preventDefault();
    setCurrentPage(1); // 검색 조건 변경 시 무조건 1페이지부터
    fetchSearchResults(1, itemsPerPage); // ✅ 현재 입력된 검색 조건으로 즉시 검색
  };

    // ✅ 페이지네이션 관련 계산
    const totalPages = Math.ceil(totalItems / itemsPerPage);
    const pageNumbers = [];
    const maxPageButtons = 10; // 화면에 보여줄 최대 페이지 버튼 수
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
                    <Label htmlFor="searchTerm">물건명</Label>
                    <Input
                        id="searchTerm"
                        type="text"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
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
                        <option value="0001">매각</option>
                        <option value="0002">임대</option>
                        {/* 추가 처분방식 코드가 있다면 여기에 추가 */}
                    </Select>
                </FormGroup>

                <FormGroup>
                    <Label htmlFor="sido">물건소재지 (시/도)</Label>
                    <Input
                        id="sido"
                        type="text"
                        value={sido}
                        onChange={(e) => setSido(e.target.value)}
                        placeholder="예: 서울특별시"
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

                <FormGroup>
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
                    <Label>최저입찰가 (원)</Label>
                    <RangeInputGroup>
                        <Input
                            type="number"
                            value={minBidPrice}
                            onChange={(e) => setMinBidPrice(e.target.value)}
                            placeholder="최소"
                        />
                        <span>~</span>
                        <Input
                            type="number"
                            value={maxBidPrice}
                            onChange={(e) => setMaxBidPrice(e.target.value)}
                            placeholder="최대"
                        />
                    </RangeInputGroup>
                </FormGroup>

                <FormGroup>
                    <Label>입찰일자</Label>
                    <RangeInputGroup>
                        <Input
                            type="date"
                            value={startDate}
                            onChange={(e) => setStartDate(e.target.value)}
                        />
                        <span>~</span>
                        <Input
                            type="date"
                            value={endDate}
                            onChange={(e) => setEndDate(e.target.value)}
                        />
                    </RangeInputGroup>
                </FormGroup>

                <FormGroup>
                    <Label htmlFor="itemsPerPage">표시 개수</Label>
                    <Select
                        id="itemsPerPage"
                        value={itemsPerPage}
                        onChange={(e) => setItemsPerPage(Number(e.target.value))}
                    >
                        <option value="10">10개</option>
                        <option value="20">20개</option>
                        <option value="50">50개</option>
                        <option value="100">100개</option>
                    </Select>
                </FormGroup>

                <SearchButton type="submit" disabled={loading}>
                    {loading ? '검색 중...' : '검색'}
                </SearchButton>
            </SearchForm>

            <ResultList>
                {loading && <LoadingMessage>검색 결과 로딩 중...</LoadingMessage>}
                {error && <ErrorMessage>{error}</ErrorMessage>}
                {searchResults.length === 0 && !loading && !error && <p>검색 결과가 없습니다.</p>}

                {searchResults.map((item) => (
                    <ResultItem key={item.tenderId}
                      onClick={() => navigate(`/tenders/${item.cltrMnmtNo}`)}
                      style={{ cursor: 'pointer' }}
                    > {/* 고유 키 사용 */}
                        <h3>{item.tenderTitle}</h3>
                        <p><strong>처분방식:</strong> {item.organization}</p>
                        <p><strong>공고번호:</strong> {item.tenderId}</p>
                        <p><strong>물건관리번호:</strong> {item.cltrMnmtNo}</p>
                        <p><strong>입찰마감일:</strong> {item.deadline ? new Date(item.deadline).toLocaleDateString() : 'N/A'}</p>
                        {/* 추가 정보 표시 가능 */}
                    </ResultItem>
                ))}
            </ResultList>
            {/* ✅ 페이지네이션 UI */}
            {!loading && !error && totalPages > 1 && (
                <PaginationContainer>
                    <PageButton onClick={() => setCurrentPage(1)} disabled={currentPage === 1}>
                        {'<<'}
                    </PageButton>
                    <PageButton onClick={() => setCurrentPage(currentPage - 1)} disabled={currentPage === 1}>
                        {'<'}
                    </PageButton>
                    {pageNumbers.map((page) => (
                        <PageButton
                            key={page}
                            onClick={() => setCurrentPage(page)}
                            active={page === currentPage}
                        >
                            {page}
                        </PageButton>
                    ))}
                    <PageButton onClick={() => setCurrentPage(currentPage + 1)} disabled={currentPage === totalPages}>
                        {'>'}
                    </PageButton>
                    <PageButton onClick={() => setCurrentPage(totalPages)} disabled={currentPage === totalPages}>
                        {'>>'}
                    </PageButton>
                </PaginationContainer>
            )}
        </SearchPageContainer>
    );
};

export default DetailSearchPage;