package com.bid.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OnbidItem {

	@JacksonXmlProperty(localName = "RNUM") private Long RNUM;
    @JacksonXmlProperty(localName = "PLNM_NO") private Long PLNM_NO;
    @JacksonXmlProperty(localName = "PBCT_NO") private Long PBCT_NO;
    @JacksonXmlProperty(localName = "PBCT_CDTN_NO") private Long PBCT_CDTN_NO;
    @JacksonXmlProperty(localName = "CLTR_NO") private Long CLTR_NO;
    @JacksonXmlProperty(localName = "CLTR_HSTR_NO") private Long CLTR_HSTR_NO;
    @JacksonXmlProperty(localName = "SCRN_GRP_CD") private String SCRN_GRP_CD;
    @JacksonXmlProperty(localName = "CTGR_FULL_NM") private String CTGR_FULL_NM;
    @JacksonXmlProperty(localName = "BID_MNMT_NO") private String BID_MNMT_NO;
    @JacksonXmlProperty(localName = "CLTR_NM") private String CLTR_NM;
    @JacksonXmlProperty(localName = "CLTR_MNMT_NO") private String CLTR_MNMT_NO;
    @JacksonXmlProperty(localName = "LDNM_ADRS") private String LDNM_ADRS;
    @JacksonXmlProperty(localName = "NMRD_ADRS") private String NMRD_ADRS;
    @JacksonXmlProperty(localName = "LDNM_PNU") private String LDNM_PNU;
    @JacksonXmlProperty(localName = "DPSL_MTD_CD") private String DPSL_MTD_CD;
    @JacksonXmlProperty(localName = "DPSL_MTD_NM") private String DPSL_MTD_NM;
    @JacksonXmlProperty(localName = "BID_MTD_NM") private String BID_MTD_NM;
    @JacksonXmlProperty(localName = "MIN_BID_PRC") private Long MIN_BID_PRC;
    @JacksonXmlProperty(localName = "APSL_ASES_AVG_AMT") private Long APSL_ASES_AVG_AMT;
    @JacksonXmlProperty(localName = "FEE_RATE") private String FEE_RATE;
    @JacksonXmlProperty(localName = "PBCT_BEGN_DTM") private String PBCT_BEGN_DTM;
    @JacksonXmlProperty(localName = "PBCT_CLS_DTM") private String PBCT_CLS_DTM;
    @JacksonXmlProperty(localName = "PBCT_CLTR_STAT_NM") private String PBCT_CLTR_STAT_NM;
    @JacksonXmlProperty(localName = "USCBD_CNT") private Long USCBD_CNT;
    @JacksonXmlProperty(localName = "IQRY_CNT") private Long IQRY_CNT;
    @JacksonXmlProperty(localName = "GOODS_NM") private String GOODS_NM;
    @JacksonXmlProperty(localName = "MANF") private String MANF;
    @JacksonXmlProperty(localName = "MDL") private String MDL;
    @JacksonXmlProperty(localName = "NRGT") private String NRGT;
    @JacksonXmlProperty(localName = "GRBX") private String GRBX;
    @JacksonXmlProperty(localName = "ENDPC") private String ENDPC;
    @JacksonXmlProperty(localName = "VHCL_MLGE") private String VHCL_MLGE;
    @JacksonXmlProperty(localName = "FUEL") private String FUEL;
    @JacksonXmlProperty(localName = "SCRT_NM") private String SCRT_NM;
    @JacksonXmlProperty(localName = "TPBZ") private String TPBZ;
    @JacksonXmlProperty(localName = "ITM_NM") private String ITM_NM;
    @JacksonXmlProperty(localName = "MMB_RGT_NM") private String MMB_RGT_NM;
    @JacksonXmlProperty(localName = "CLTR_IMG_FILES") private String CLTR_IMG_FILES;
}
