package com.bid.service;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.bid.dto.response.TenderResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component // Spring Bean으로 등록
@Slf4j
public class OnbidApiParser {

	private static final DateTimeFormatter ONBID_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	@Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor // Lombok NoArgsConstructor 추가
    public static class TenderListResult { // 내부 클래스가 아니라 public static으로
        private List<TenderResponseDTO> tenders;
        private int totalCount;
    }
	
	public TenderListResult parseXmlToTenderDtosAndCount(String xmlString) {
        List<TenderResponseDTO> dtoList = new ArrayList();
        int totalCount = 0;

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xmlString)));
            doc.getDocumentElement().normalize();

            NodeList bodyNodes = doc.getElementsByTagName("body");
            if (bodyNodes.getLength() > 0) {
                Element bodyElement = (Element) bodyNodes.item(0);
                String totalCountStr = getTagValue("totalCount", bodyElement);

                if (totalCountStr != null && !totalCountStr.isEmpty()) {
                    try { totalCount = Integer.parseInt(totalCountStr); }
                    catch (NumberFormatException e) { log.warn("TotalCount 값 '{}'이 유효한 숫자가 아닙니다.", totalCountStr); }
                } else { log.warn("XML 응답 바디에서 'totalCount' 태그를 찾을 수 없거나 비어 있습니다."); }
            } else { log.warn("XML 응답에서 'body' 태그를 찾을 수 없습니다."); }

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
                    String goodsName = getTagValue("GOODS_NM", element);

                    Long plnmNo = (plnmNoStr != null && !plnmNoStr.isEmpty()) ? Long.parseLong(plnmNoStr) : null;
                    Long pbctNo = (pbctNoStr != null && !pbctNoStr.isEmpty()) ? Long.parseLong(pbctNoStr) : null;

                    dtoList.add(TenderResponseDTO.builder()
                            .tenderId(plnmNo)
                            .pbctNo(pbctNo)
                            .cltrHstrNo(cltrHstrNoStr)
                            .cltrMnmtNo(cltrMnmtNoStr)
                            .tenderTitle(cltrNm)
                            .organization(dpslMtdNm)
                            .bidNumber(bidMnmtNo)
                            .goodsName(goodsName)
                            .announcementDate(parseDateTime(pbctBegnDtm))
                            .deadline(parseDateTime(pbctClsDtm))
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("XML 파싱 중 오류 발생: {}", e.getMessage(), e);
        }
        return new TenderListResult(dtoList, totalCount);
    }

    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            if (node != null && node.getFirstChild() != null) {
                return node.getFirstChild().getNodeValue();
            }
        }
        return null;
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeString, ONBID_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date-time string: '{}'. Error: {}", dateTimeString, e.getMessage());
            return null;
        }
    }
}
