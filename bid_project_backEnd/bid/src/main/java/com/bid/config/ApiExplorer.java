package com.bid.config;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.IOException;

public class ApiExplorer {
    public static void main(String[] args) throws IOException {
        StringBuilder urlBuilder = new StringBuilder("http://openapi.onbid.co.kr/openapi/services/KamcoPblsalThingInquireSvc/getKamcoPbctCltrList"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=서비스키"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /*페이지당 데이터 개수*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지 번호*/
        urlBuilder.append("&" + URLEncoder.encode("DPSL_MTD_CD","UTF-8") + "=" + URLEncoder.encode("0001", "UTF-8")); /*0001 매각 0002 임대*/
        urlBuilder.append("&" + URLEncoder.encode("CTGR_HIRK_ID","UTF-8") + "=" + URLEncoder.encode("10000", "UTF-8")); /*코드조회 오퍼레이션 참조*/
        urlBuilder.append("&" + URLEncoder.encode("CTGR_HIRK_ID_MID","UTF-8") + "=" + URLEncoder.encode("10100", "UTF-8")); /*코드조회 오퍼레이션 참조*/
        urlBuilder.append("&" + URLEncoder.encode("SIDO","UTF-8") + "=" + URLEncoder.encode("강원도", "UTF-8")); /*물건소재지(시도)*/
        urlBuilder.append("&" + URLEncoder.encode("SGK","UTF-8") + "=" + URLEncoder.encode("인제군", "UTF-8")); /*물건소재지(시군구)*/
        urlBuilder.append("&" + URLEncoder.encode("EMD","UTF-8") + "=" + URLEncoder.encode("남면", "UTF-8")); /*물건소재지(읍면동)*/
        urlBuilder.append("&" + URLEncoder.encode("GOODS_PRICE_FROM","UTF-8") + "=" + URLEncoder.encode("522740000", "UTF-8")); /*감정가하한*/
        urlBuilder.append("&" + URLEncoder.encode("GOODS_PRICE_TO","UTF-8") + "=" + URLEncoder.encode("617393000", "UTF-8")); /*감정가상한*/
        urlBuilder.append("&" + URLEncoder.encode("OPEN_PRICE_FROM","UTF-8") + "=" + URLEncoder.encode("522740000", "UTF-8")); /*최저입찰가하한*/
        urlBuilder.append("&" + URLEncoder.encode("OPEN_PRICE_TO","UTF-8") + "=" + URLEncoder.encode("617393000", "UTF-8")); /*최저입찰가상한*/
        urlBuilder.append("&" + URLEncoder.encode("CLTR_NM","UTF-8") + "=" + URLEncoder.encode("종이팩", "UTF-8")); /*물건명*/
        urlBuilder.append("&" + URLEncoder.encode("PBCT_BEGN_DTM","UTF-8") + "=" + URLEncoder.encode("20171218", "UTF-8")); /*YYYYMMDD*/
        urlBuilder.append("&" + URLEncoder.encode("PBCT_CLS_DTM","UTF-8") + "=" + URLEncoder.encode("20171218", "UTF-8")); /*YYYYMMDD*/
        urlBuilder.append("&" + URLEncoder.encode("CLTR_MNMT_NO","UTF-8") + "=" + URLEncoder.encode("2012-1141-001291", "UTF-8")); /*물건관리번호*/
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        System.out.println(sb.toString());
    }
}
