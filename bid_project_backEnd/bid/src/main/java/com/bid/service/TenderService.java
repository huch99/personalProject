package com.bid.service;

import java.io.StringReader;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.bid.dto.response.TenderResponseDTO;
import lombok.RequiredArgsConstructor;
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

	public List<TenderResponseDTO> getAllTenders() {
		long startTime = System.currentTimeMillis();
        // 1. 온비드 API 호출을 위한 URI 구성
        URI uri = UriComponentsBuilder.fromUriString(onbidApiBaseUrl)
                .queryParam("serviceKey", onbidApiServiceKey)
                .queryParam("pageNo", 1) 
                .queryParam("numOfRows", 10) 
                .queryParam("DPSL_MTD_CD", "0001")
                .queryParam("sort", "PBCT_BEGN_DTM") // 예를 들어 공고 시작일 기준
                .queryParam("order", "DESC") // 내림차순 (최신순)
                .encode()
                .build().toUri();

        log.info("Calling Onbid API: {}", uri); 

        try {
        	long apiCallStartTime = System.currentTimeMillis();
        	
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);
            
            long apiCallEndTime = System.currentTimeMillis();
            log.info("온비드 API 응답 수신 시간: {}ms", (apiCallEndTime - apiCallStartTime));
            
            // ✅ 3. 응답 처리 및 XML 데이터 파싱
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                String rawXmlResponse = responseEntity.getBody();
                long xmlParsingStartTime = System.currentTimeMillis();
                log.info("Onbid API Raw XML Response: {}", rawXmlResponse); // 실제 XML 응답 로그

                List<TenderResponseDTO> dtoList = parseXmlToTenderDtos(rawXmlResponse);
                long xmlParsingEndTime = System.currentTimeMillis();
                log.info("XML 파싱 및 DTO 매핑 시간: {}ms", (xmlParsingEndTime - xmlParsingStartTime));
                
                if (dtoList.isEmpty()) {
                    log.warn("Onbid API 응답을 파싱했지만 DTO 리스트가 비어 있습니다.");
                }
                return dtoList;
            } else {
                String statusCode = responseEntity.getStatusCode().toString();
                String errorMessage = String.format("Onbid API 호출 실패: HTTP Status %s, Response Body: %s", 
                                                    statusCode, responseEntity.getBody());
                log.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        } catch (Exception e) {
            log.error("Onbid API 호출 중 예외 발생: {}", e.getMessage(), e);
            throw new RuntimeException("온비드 입찰 정보를 불러오는데 실패했습니다.", e);
        } finally {
        	long endTime = System.currentTimeMillis();
            log.info("getAllTenders 메서드 총 실행 시간: {}ms", (endTime - startTime));
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

		List<Map<String, String>> parsedItems = performXmlParsing(xmlString);

		for (Map<String, String> itemData : parsedItems) {
			TenderResponseDTO dto = new TenderResponseDTO();

			// --- String 값을 Long으로 변환하는 부분 ---
			String plnmNoStr = itemData.get("PLNM_NO");
			if (plnmNoStr != null && !plnmNoStr.isEmpty()) {
				try {
					dto.setTenderId(Long.parseLong(plnmNoStr));
				} catch (NumberFormatException e) {
					System.err.println("PLNM_NO 숫자로 변환 실패: " + plnmNoStr);
					dto.setTenderId(null);
				}
			} else {
				dto.setTenderId(null);
			}

			String pbctNoStr = itemData.get("PBCT_NO");
			if (pbctNoStr != null && !pbctNoStr.isEmpty()) {
				try {
					dto.setPbctNo(Long.parseLong(pbctNoStr));
				} catch (NumberFormatException e) {
					System.err.println("PBCT_NO 숫자로 변환 실패: " + pbctNoStr);
					dto.setPbctNo(null);
				}
			} else {
				dto.setPbctNo(null);
			}

			dto.setCltrHstrNo(itemData.get("CLTR_HSTR_NO")); // String 타입은 변환 필요 없음

			dto.setTenderTitle(itemData.get("CLTR_NM"));
			dto.setOrganization(itemData.get("DPSL_MTD_NM"));
			dto.setBidNumber(itemData.get("BID_MNMT_NO"));

			// 날짜 변환 로직 (LocalDateTime)
			String pbctBegnDtm = itemData.get("PBCT_BEGN_DTM");
			if (pbctBegnDtm != null && pbctBegnDtm.length() == 14) {
				try {
					dto.setAnnouncementDate(
							LocalDateTime.parse(pbctBegnDtm, DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
				} catch (Exception e) {
					System.err.println("공고일 날짜 변환 오류: " + pbctBegnDtm);
					dto.setAnnouncementDate(null);
				}
			}

			String pbctClsDtm = itemData.get("PBCT_CLS_DTM");
			if (pbctClsDtm != null && pbctClsDtm.length() == 14) {
				try {
					dto.setDeadline(LocalDateTime.parse(pbctClsDtm, DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
				} catch (Exception e) {
					System.err.println("마감일 날짜 변환 오류: " + pbctClsDtm);
					dto.setDeadline(null);
				}
			}

			dtoList.add(dto);
		}
		return dtoList;
	}

	private List<Map<String, String>> performXmlParsing(String xmlString) {
		List<Map<String, String>> parsedItems = new ArrayList<>();

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlString));
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();

			NodeList itemNodes = doc.getElementsByTagName("item");

			for (int i = 0; i < itemNodes.getLength(); i++) {
				Node itemNode = itemNodes.item(i);

				if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
					Element itemElement = (Element) itemNode;
					Map<String, String> itemMap = new HashMap<>();

					// Huch님의 XML 데이터에서 필요한 모든 태그의 값을 추출하여 itemMap에 추가합니다.
					// 모든 태그 이름에 대해 getTagValue를 호출하고 map에 put 합니다.
					itemMap.put("RNUM", getTagValue("RNUM", itemElement));
					itemMap.put("PLNM_NO", getTagValue("PLNM_NO", itemElement));
					itemMap.put("PBCT_NO", getTagValue("PBCT_NO", itemElement));
					itemMap.put("PBCT_CDTN_NO", getTagValue("PBCT_CDTN_NO", itemElement));
					itemMap.put("CLTR_NO", getTagValue("CLTR_NO", itemElement));
					itemMap.put("CLTR_HSTR_NO", getTagValue("CLTR_HSTR_NO", itemElement));
					itemMap.put("SCRN_GRP_CD", getTagValue("SCRN_GRP_CD", itemElement));
					itemMap.put("CTGR_FULL_NM", getTagValue("CTGR_FULL_NM", itemElement));
					itemMap.put("BID_MNMT_NO", getTagValue("BID_MNMT_NO", itemElement));
					itemMap.put("CLTR_NM", getTagValue("CLTR_NM", itemElement));
					itemMap.put("CLTR_MNMT_NO", getTagValue("CLTR_MNMT_NO", itemElement));
					itemMap.put("LDNM_ADRS", getTagValue("LDNM_ADRS", itemElement));
					itemMap.put("NMRD_ADRS", getTagValue("NMRD_ADRS", itemElement));
					itemMap.put("LDNM_PNU", getTagValue("LDNM_PNU", itemElement));
					itemMap.put("DPSL_MTD_CD", getTagValue("DPSL_MTD_CD", itemElement));
					itemMap.put("DPSL_MTD_NM", getTagValue("DPSL_MTD_NM", itemElement));
					itemMap.put("BID_MTD_NM", getTagValue("BID_MTD_NM", itemElement));
					itemMap.put("MIN_BID_PRC", getTagValue("MIN_BID_PRC", itemElement));
					itemMap.put("APSL_ASES_AVG_AMT", getTagValue("APSL_ASES_AVG_AMT", itemElement));
					itemMap.put("FEE_RATE", getTagValue("FEE_RATE", itemElement));
					itemMap.put("PBCT_BEGN_DTM", getTagValue("PBCT_BEGN_DTM", itemElement));
					itemMap.put("PBCT_CLS_DTM", getTagValue("PBCT_CLS_DTM", itemElement));
					itemMap.put("PBCT_CLTR_STAT_NM", getTagValue("PBCT_CLTR_STAT_NM", itemElement));
					itemMap.put("USCBD_CNT", getTagValue("USCBD_CNT", itemElement));
					itemMap.put("IQRY_CNT", getTagValue("IQRY_CNT", itemElement));
					itemMap.put("GOODS_NM", getTagValue("GOODS_NM", itemElement));
					itemMap.put("MANF", getTagValue("MANF", itemElement));
					itemMap.put("MDL", getTagValue("MDL", itemElement));
					itemMap.put("NRGT", getTagValue("NRGT", itemElement));
					itemMap.put("GRBX", getTagValue("GRBX", itemElement));
					itemMap.put("ENDPC", getTagValue("ENDPC", itemElement));
					itemMap.put("VHCL_MLGE", getTagValue("VHCL_MLGE", itemElement));
					itemMap.put("FUEL", getTagValue("FUEL", itemElement));
					itemMap.put("SCRT_NM", getTagValue("SCRT_NM", itemElement));
					itemMap.put("TPBZ", getTagValue("TPBZ", itemElement));
					itemMap.put("ITM_NM", getTagValue("ITM_NM", itemElement));
					itemMap.put("MMB_RGT_NM", getTagValue("MMB_RGT_NM", itemElement));
					itemMap.put("CLTR_IMG_FILES", getTagValue("CLTR_IMG_FILES", itemElement));

					parsedItems.add(itemMap);
				}
			}

		} catch (Exception e) {
			System.err.println("XML 파싱 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}
		return parsedItems;
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

}
