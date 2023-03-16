<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright 2023 Exactpro Systems Limited -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:iso="urn:iso:std:iso:20022:tech:xsd:pain.001.001.09">
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="no" />
    <xsl:template match="/">
        <xsl:apply-templates select="iso:CstmrCdtTrfInitn" />
    </xsl:template>
    <xsl:template match="iso:CdtTrfTxInf">
        <CdtTrfTxInf>
            <PmtIdEndToEndId><xsl:value-of select="iso:PmtId/iso:EndToEndId" /></PmtIdEndToEndId>
        </CdtTrfTxInf>
    </xsl:template>
    <xsl:template match="iso:CstmrCdtTrfInitn">
        <CstmrCdtTrfInitn>
            <xsl:apply-templates select="iso:PmtInf" />
        </CstmrCdtTrfInitn>
    </xsl:template>
    <xsl:template match="iso:Othr">
        <DbtrIdOrgIdOthrId>
            <xsl:value-of select="iso:Id" />
        </DbtrIdOrgIdOthrId>
    </xsl:template>
    <xsl:template match="iso:PmtInf">
        <PmtInf>
            <xsl:apply-templates select="iso:CdtTrfTxInf|iso:Dbtr/iso:Id/iso:OrgId/iso:Othr" />
        </PmtInf>
    </xsl:template>
</xsl:stylesheet>
