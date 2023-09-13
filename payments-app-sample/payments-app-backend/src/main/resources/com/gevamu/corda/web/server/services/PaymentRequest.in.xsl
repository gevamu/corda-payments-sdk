<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pain="urn:iso:std:iso:20022:tech:xsd:pain.001.001.09"
                version="1.0">
    <xsl:output method="xml" encoding="UTF-8" indent="no"/>

    <xsl:template match="pain:DbtrAcct|pain:CdtrAcct">
        <Currency><xsl:value-of select="./pain:Ccy"/></Currency>
        <Account><xsl:value-of select="./pain:Id/pain:Othr/pain:Id"/></Account>
        <AccountName><xsl:value-of select="./pain:Nm"/></AccountName>
    </xsl:template>

    <xsl:template match="/pain:CstmrCdtTrfInitn">
        <PaymentRequest>
            <Creditor>
                <Bic><xsl:value-of select="./pain:PmtInf/pain:CdtTrfTxInf/pain:CdtrAgt/pain:FinInstnId/pain:BICFI"/></Bic>
                <Country><xsl:value-of select="./pain:PmtInf/pain:CdtTrfTxInf/pain:Cdtr/pain:PstlAdr/pain:Ctry"/></Country>
                <xsl:apply-templates select="./pain:PmtInf/pain:CdtTrfTxInf/pain:CdtrAcct"/>
            </Creditor>
            <Debtor>
                <Bic><xsl:value-of select="./pain:PmtInf/pain:DbtrAgt/pain:FinInstnId/pain:BICFI"/></Bic>
                <Country><xsl:value-of select="./pain:PmtInf/pain:Dbtr/pain:PstlAdr/pain:Ctry"/></Country>
                <xsl:apply-templates select="./pain:PmtInf/pain:DbtrAcct"/>
            </Debtor>
            <Amount><xsl:value-of select="./pain:PmtInf/pain:CdtTrfTxInf/pain:Amt/pain:InstdAmt"/></Amount>
            <ParticipantId><xsl:value-of select="./pain:PmtInf/pain:Dbtr/pain:Id/pain:OrgId/pain:Othr/pain:Id"/></ParticipantId>
            <MsgId><xsl:value-of select="./pain:GrpHdr/pain:MsgId"/></MsgId>
            <PmtInfId><xsl:value-of select="./pain:PmtInf/pain:PmtInfId"/></PmtInfId>
            <InstrId><xsl:value-of select="./pain:PmtInf/pain:CdtTrfTxInf/pain:PmtInf/pain:InstrId"/></InstrId>
            <EndToEndId><xsl:value-of select="./pain:PmtInf/pain:CdtTrfTxInf/pain:PmtInf/pain:EndToEndId"/></EndToEndId>
            <CreDtTm><xsl:value-of select="./pain:GrpHdr/pain:CreDtTm"/></CreDtTm>
            <ReqdExctnDt><xsl:value-of select="./pain:PmtInf/pain:ReqdExctnDt/pain:Dt"/></ReqdExctnDt>
        </PaymentRequest>
    </xsl:template>

</xsl:stylesheet>
