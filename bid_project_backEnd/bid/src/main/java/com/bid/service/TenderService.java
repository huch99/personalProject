package com.bid.service;

import java.io.StringReader;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import com.bid.dto.OnbidItem;
import com.bid.dto.response.PagedTenderResponse;
import com.bid.dto.response.TenderResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TenderService {

	private final RestTemplate restTemplate;

	@Value("${onbid.api.base-url}")
	private String onbidApiBaseUrl;

	@Value("${onbid.api.service-key}")
	private String onbidApiServiceKey;

	private static final DateTimeFormatter ONBID_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	public List<TenderResponseDTO> getAllTenders() {
		// 1. 온비드 API 호출을 위한 URI 구성
		URI uri = UriComponentsBuilder.fromUriString(onbidApiBaseUrl).queryParam("serviceKey", onbidApiServiceKey)
				.queryParam("pageNo", 1).queryParam("numOfRows", 100).queryParam("DPSL_MTD_CD", "0001")
				.queryParam("sort", "PBCT_BEGN_DTM") // 예를 들어 공고 시작일 기준
				.encode().build().toUri();

		try {
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

			// ✅ 3. 응답 처리 및 XML 데이터 파싱
			if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
				String rawXmlResponse = responseEntity.getBody();

				List<TenderResponseDTO> dtoList = parseXmlToTenderDtos(rawXmlResponse);

				List<TenderResponseDTO> filteredList = filterDuplicateTenders(dtoList);

				if (filteredList.isEmpty()) {
					log.warn("Onbid API 응답을 파싱하고 필터링했지만 DTO 리스트가 비어 있습니다.");
				}
				return filteredList;
			} else {
				String statusCode = responseEntity.getStatusCode().toString();
				String errorMessage = String.format("Onbid API 호출 실패: HTTP Status %s, Response Body: %s", statusCode,
						responseEntity.getBody());
				log.error(errorMessage);
				throw new RuntimeException(errorMessage);
			}
		} catch (Exception e) {
			log.error("Onbid API 호출 중 예외 발생: {}", e.getMessage(), e);
			throw new RuntimeException("온비드 입찰 정보를 불러오는데 실패했습니다.", e);
		}
	}

	private TenderResponseDTO mapOnbidItemToTenderResponseDTO(OnbidItem onbidItem) {
		// 공고일/마감일이 String으로 오는 경우, 파싱 로직 필요
		LocalDateTime announcementDate = parseDateTime(onbidItem.getPBCT_BEGN_DTM());
		LocalDateTime deadline = parseDateTime(onbidItem.getPBCT_CLS_DTM());

		return TenderResponseDTO.builder()
				// TODO: 실제 온비드 Item의 필드명과 TenderResponseDto의 필드명 매핑
				.tenderId(onbidItem.getPLNM_NO()) // 고유 ID 생성 방식 고민 (여기서는 bidNo 기반으로 임시 생성)
				.tenderTitle(onbidItem.getCLTR_NM()).organization(onbidItem.getDPSL_MTD_NM())
				.bidNumber(onbidItem.getBID_MNMT_NO()).announcementDate(announcementDate).deadline(deadline).build();
	}

	// String 형태의 날짜/시간을 LocalDateTime으로 파싱 (API 문서의 날짜 형식 확인 필요)
	private LocalDateTime parseDateTime(String dateTimeString) {
		if (dateTimeString == null || dateTimeString.isEmpty()) {
			return null;
		}
		// TODO: 온비드 API 문서에 명시된 정확한 날짜/시간 포맷으로 DateTimeFormatter 수정
		// 예: "yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss" 등
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss"); // 임시 포맷
		try {
			return LocalDateTime.parse(dateTimeString, formatter);
		} catch (DateTimeParseException e) {
			log.warn("날짜 파싱 실패: {} (필요 포맷: yyyyMMddHHmmss)", dateTimeString);
			// 다른 포맷 시도 또는 null 반환 또는 기본값 설정
			return null;
		}
	}

	// 온비드 bidNo는 문자열일 수 있으므로, 우리 서비스의 Long 타입 ID로 변환/생성 로직 필요
	// 여기서는 간단히 문자열의 해시값 또는 일부를 이용하는 예시 (실제 서비스에서는 더 견고한 전략 필요)
	private Long generateUniqueId(String bidNo) {
		if (bidNo == null)
			return null;
		try {
			// bidNo가 순수한 숫자 문자열이면 Long으로 파싱
			return Long.parseLong(bidNo);
		} catch (NumberFormatException e) {
			// bidNo가 숫자 이외의 문자를 포함한다면, 예를 들어 해시코드를 이용
			return (long) bidNo.hashCode(); // 단순 예시. 실제 고유성을 보장하려면 다른 방법 모색
		}
	}

	public List<TenderResponseDTO> parseXmlToTenderDtos(String xmlString) {
		List<TenderResponseDTO> dtoList = new ArrayList<>();

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlString));
			is.setEncoding("UTF-8"); // ✅ 인코딩 문제 발생 시 대비 (기본적으로 UTF-8이겠지만 명시)
			Document doc = builder.parse(is);
			doc.getDocumentElement().normalize();

			NodeList itemList = doc.getElementsByTagName("item"); // XML 응답에서 <item> 태그를 찾습니다.

			for (int i = 0; i < itemList.getLength(); i++) {
				Node itemNode = itemList.item(i);
				if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) itemNode;

					// ✅ XML 응답에서 가져올 태그명과 DTO 필드명을 매핑
					// 이 부분의 태그명은 Huch님의 실제 온비드 API XML 응답 태그명과 100% 일치해야 합니다.
					String plnmNoStr = getTagValue("PLNM_NO", element); // 공고번호 (tenderId)
					String pbctNoStr = getTagValue("PBCT_NO", element); // 공매번호 (pbctNo)
					String cltrHstrNoStr = getTagValue("CLTR_HSTR_NO", element); // 물건이력번호 (cltrHstrNo)
					String cltrMnmtNoStr = getTagValue("CLTR_MNMT_NO", element); // 물건관리번호 (cltrMnmtNo)
					String cltrNm = getTagValue("CLTR_NM", element); // 물건명 (tenderTitle)
					String dpslMtdNm = getTagValue("DPSL_MTD_NM", element); // 처분방식코드명 (organization)
					String bidMnmtNo = getTagValue("BID_MNMT_NO", element); // 입찰번호 (bidNumber)
					String pbctBegnDtm = getTagValue("PBCT_BEGN_DTM", element); // 입찰시작일시 (announcementDate)
					String pbctClsDtm = getTagValue("PBCT_CLS_DTM", element); // 입찰마감일시 (deadline)

					// 숫자 필드는 Long으로 파싱, null 체크 및 파싱 오류 처리
					Long plnmNo = (plnmNoStr != null && !plnmNoStr.isEmpty()) ? Long.parseLong(plnmNoStr) : null;
					Long pbctNo = (pbctNoStr != null && !pbctNoStr.isEmpty()) ? Long.parseLong(pbctNoStr) : null;

					dtoList.add(TenderResponseDTO.builder().tenderId(plnmNo) // 공고번호 (Long)
							.pbctNo(pbctNo) // 공매번호 (Long)
							.cltrHstrNo(cltrHstrNoStr) // 물건이력번호 (String)
							.cltrMnmtNo(cltrMnmtNoStr) // ✅ 물건관리번호 (String)
							.tenderTitle(cltrNm).organization(dpslMtdNm).bidNumber(bidMnmtNo)
							.announcementDate(parseDateTime(pbctBegnDtm)).deadline(parseDateTime(pbctClsDtm)).build());
				}
			}
		} catch (Exception e) {
			log.error("XML 파싱 중 오류 발생: {}", e.getMessage(), e);
			return Collections.emptyList();
		}
		return dtoList;
	}

	private String getTagValue(String tag, Element element) {
		NodeList nl = element.getElementsByTagName(tag);
		if (nl != null && nl.getLength() > 0) {
			Node node = nl.item(0);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element innerElement = (Element) node;
				NodeList childNodes = innerElement.getChildNodes();
				if (childNodes != null && childNodes.getLength() > 0) {
					return childNodes.item(0).getNodeValue();
				}
			}
		}
		return null;
	}

	private List<TenderResponseDTO> filterDuplicateTenders(List<TenderResponseDTO> tenderList) {
		if (tenderList == null || tenderList.isEmpty()) {
			return Collections.emptyList();
		}

		Map<String, TenderResponseDTO> uniqueTendersMap = new HashMap<>();

		for (TenderResponseDTO currentTender : tenderList) {
			String cltrMnmtNo = currentTender.getCltrMnmtNo(); // ✅ 이제 DTO에 cltrMnmtNo 필드가 있습니다.

			if (cltrMnmtNo == null || cltrMnmtNo.isEmpty()) {
				// CLTR_MNMT_NO가 없으면 고유한 물건으로 간주하거나, 스킵할 수 있습니다.
				// 여기서는 고유한 것으로 간주하고 UUID를 키로 사용하여 맵에 추가
				log.warn("CLTR_MNMT_NO가 없는 입찰 항목이 발견되었습니다. 임시 고유 키로 추가합니다.");
				uniqueTendersMap.put(UUID.randomUUID().toString(), currentTender);
				continue;
			}

			if (uniqueTendersMap.containsKey(cltrMnmtNo)) {
				// 이미 해당 물건관리번호가 맵에 있다면, 마감일시를 비교하여 더 최신 항목으로 업데이트
				TenderResponseDTO existingTender = uniqueTendersMap.get(cltrMnmtNo);

				// 현재 입찰 마감일시가 기존 항목의 마감일시보다 늦으면 업데이트
				if (currentTender.getDeadline() != null && existingTender.getDeadline() != null
						&& currentTender.getDeadline().isAfter(existingTender.getDeadline())) {
					uniqueTendersMap.put(cltrMnmtNo, currentTender);
				} else if (currentTender.getDeadline() != null && existingTender.getDeadline() == null) {
					// 현재 항목의 날짜는 유효하고 기존 항목의 날짜는 유효하지 않다면 현재 항목으로 업데이트
					uniqueTendersMap.put(cltrMnmtNo, currentTender);
				}
				// 기존 항목이 더 최신이거나 같으면 기존 항목 유지
			} else {
				// 처음 발견된 물건관리번호이므로 바로 추가
				uniqueTendersMap.put(cltrMnmtNo, currentTender);
			}
		}

		return new ArrayList<>(uniqueTendersMap.values());
	}

	public PagedTenderResponse searchTenders(String cltrNm, String dpslMtdCd, String sido, String sgk, String emd,
			String goodsPriceFrom, String goodsPriceTo, String openPriceFrom, String openPriceTo, String pbctBegnDtm,
			String pbctClsDtm, int pageNo, int numOfRows) {

		long startTime = System.currentTimeMillis();
		
		// ✅ 1단계: 프론트엔드에서 넘어온 검색 조건 로그
        log.info("Received search parameters - cltrNm: '{}', dpslMtdCd: '{}', sido: '{}', sgk: '{}', emd: '{}'",
                 cltrNm, dpslMtdCd, sido, sgk, emd);
        log.info("Received search prices - goodsPriceFrom: '{}', goodsPriceTo: '{}', openPriceFrom: '{}', openPriceTo: '{}'",
                 goodsPriceFrom, goodsPriceTo, openPriceFrom, openPriceTo);
        log.info("Received search dates - pbctBegnDtm: '{}', pbctClsDtm: '{}'", pbctBegnDtm, pbctClsDtm);
        log.info("Received paging - pageNo: {}, numOfRows: {}", pageNo, numOfRows);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(onbidApiBaseUrl)
				.queryParam("serviceKey", onbidApiServiceKey).queryParam("pageNo", pageNo)
				.queryParam("numOfRows", numOfRows);

		// ✅ 검색 조건이 있을 경우에만 쿼리 파라미터로 추가
		if (cltrNm != null && !cltrNm.isEmpty())
			uriBuilder.queryParam("CLTR_NM", cltrNm);
		if (dpslMtdCd != null && !dpslMtdCd.isEmpty())
			uriBuilder.queryParam("DPSL_MTD_CD", dpslMtdCd);
		if (sido != null && !sido.isEmpty())
			uriBuilder.queryParam("SIDO", sido);
		if (sgk != null && !sgk.isEmpty())
			uriBuilder.queryParam("SGK", sgk);
		if (emd != null && !emd.isEmpty())
			uriBuilder.queryParam("EMD", emd);
		if (goodsPriceFrom != null && !goodsPriceFrom.isEmpty())
			uriBuilder.queryParam("GOODS_PRICE_FROM", goodsPriceFrom);
		if (goodsPriceTo != null && !goodsPriceTo.isEmpty())
			uriBuilder.queryParam("GOODS_PRICE_TO", goodsPriceTo);
		if (openPriceFrom != null && !openPriceFrom.isEmpty())
			uriBuilder.queryParam("OPEN_PRICE_FROM", openPriceFrom);
		if (openPriceTo != null && !openPriceTo.isEmpty())
			uriBuilder.queryParam("OPEN_PRICE_TO", openPriceTo);
		if (pbctBegnDtm != null && !pbctBegnDtm.isEmpty())
			uriBuilder.queryParam("PBCT_BEGN_DTM", pbctBegnDtm);
		if (pbctClsDtm != null && !pbctClsDtm.isEmpty())
			uriBuilder.queryParam("PBCT_CLS_DTM", pbctClsDtm);

		URI uri = uriBuilder.encode().build().toUri();

		log.info("Calling Onbid API for detailed search: {}", uri);

		try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                String rawXmlResponse = responseEntity.getBody();
                
                // ✅ XML 파싱 결과를 TenderListResult 객체로 받음
                TenderListResult parsedResult = parseXmlToTenderDtosAndCount(rawXmlResponse);
                List<TenderResponseDTO> dtoList = parsedResult.getTenders(); // 실제 DTO 목록
                int totalCount = parsedResult.getTotalCount(); // 총 건수

                List<TenderResponseDTO> filteredList = filterDuplicateTenders(dtoList); // 중복 제거 필터링

                log.info("상세 검색 결과 (필터링 후): {}건, 총 건수: {}", filteredList.size(), totalCount);
                
                // ✅ PagedTenderResponse 객체로 묶어서 반환
                return PagedTenderResponse.builder()
                        .tenders(filteredList)
                        .totalCount(totalCount)
                        .pageNo(pageNo)
                        .numOfRows(numOfRows)
                        .build();

            } else {
                String statusCode = responseEntity.getStatusCode().toString();
                String errorMessage = String.format("Onbid API 상세 검색 호출 실패: HTTP Status %s, Response Body: %s",
                                                    statusCode, responseEntity.getBody());
                log.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        } catch (Exception e) {
            log.error("Onbid API 상세 검색 중 예외 발생: {}", e.getMessage(), e);
            throw new RuntimeException("온비드 입찰 정보를 불러오는데 실패했습니다.", e);
        } finally {
        	long endTime = System.currentTimeMillis();
            log.info("searchTenders 메서드 총 실행 시간: {}ms", (endTime - startTime));
        }
    }

    // ✅ XML 파싱 결과를 TenderListResult 객체로 받도록 수정된 parseXmlToTenderDtos 메서드
    // 이름도 더 명확하게 변경합니다. (기존 메서드 삭제 후 이 메서드 사용)
    private TenderListResult parseXmlToTenderDtosAndCount(String xmlString) {
		List<TenderResponseDTO> dtoList = new ArrayList<>();
        int totalCount = 0; // 총 건수 초기화
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlString));
            is.setEncoding("UTF-8");
			Document doc = builder.parse(is);
			doc.getDocumentElement().normalize();

            // ✅ TotalCount 파싱
            String totalCountStr = getTagValue("TotalCount", doc.getDocumentElement());
            if (totalCountStr != null && !totalCountStr.isEmpty()) {
                totalCount = Integer.parseInt(totalCountStr);
            } else {
                log.warn("XML 응답에서 TotalCount를 찾을 수 없거나 비어 있습니다.");
            }


			NodeList itemList = doc.getElementsByTagName("item");

			for (int i = 0; i < itemList.getLength(); i++) {
				Node itemNode = itemList.item(i);
				if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) itemNode;

					String plnmNoStr = getTagValue("PLNM_NO", element);
                    String pbctNoStr = getTagValue("PBCT_NO", element);
                    String cltrHstrNoStr = getTagValue("CLTR_HSTR_NO", element);
					String cltrMnmtNoStr = getTagValue("CLTR_MNMT_NO", element);
					String cltrNm = getTagValue("CLTR_NM", element);
					String dpslMtdNm = getTagValue("DPSL_MTD_NM", element);
					String bidMnmtNo = getTagValue("BID_MNMT_NO", element);
					String pbctBegnDtm = getTagValue("PBCT_BEGN_DTM", element);
					String pbctClsDtm = getTagValue("PBCT_CLS_DTM", element);

                    Long plnmNo = (plnmNoStr != null && !plnmNoStr.isEmpty()) ? Long.parseLong(plnmNoStr) : null;
                    Long pbctNo = (pbctNoStr != null && !pbctNoStr.isEmpty()) ? Long.parseLong(pbctNoStr) : null;
//                    Long cltrHstrNo = (cltrHstrNoStr != null && !cltrHstrNoStr.isEmpty()) ? Long.parseLong(cltrHstrNoStr) : null;


					dtoList.add(TenderResponseDTO.builder()
							.tenderId(plnmNo)
                            .pbctNo(pbctNo)
                            .cltrHstrNo(cltrHstrNoStr)
							.cltrMnmtNo(cltrMnmtNoStr)
							.tenderTitle(cltrNm)
							.organization(dpslMtdNm)
							.bidNumber(bidMnmtNo)
							.announcementDate(parseDateTime(pbctBegnDtm))
							.deadline(parseDateTime(pbctClsDtm))
							.build());
				}
			}
		} catch (Exception e) {
			log.error("XML 파싱 중 오류 발생: {}", e.getMessage(), e);
			// 파싱 오류 시에도 totalCount는 0으로 반환될 수 있음
		}
		// ✅ 총 건수와 DTO 목록을 함께 반환하는 내부 클래스 인스턴스 생성
		return new TenderListResult(dtoList, totalCount);
	}

    @Getter
    @Setter
    @AllArgsConstructor
    private static class TenderListResult {
        private List<TenderResponseDTO> tenders;
        private int totalCount;
    }
}
