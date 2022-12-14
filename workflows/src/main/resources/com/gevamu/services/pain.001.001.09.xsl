<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

    <xsl:template match="/Document">
        <xsl:for-each select="CstmrCdtTrfInitn">
            <PaymentXmlData>
                <Time> <xsl:value-of select="GrpHdr/CreDtTm"/> </Time>
                <PaymentInformation>
                    <xsl:for-each select="PmtInf/CdtTrfTxInf">

                        <EndToEndId> <xsl:value-of select="PmtId/EndToEndId"/> </EndToEndId>
                        <Amount> <xsl:value-of select="Amt/InstdAmt"/> </Amount>
                        <Currency> <xsl:value-of select="Amt/InstdAmt/@Ccy"/> </Currency>

                        <Creditor>
                            <AccountName> <xsl:value-of select="Cdtr/Nm"/> </AccountName>
                            <AccountId> <xsl:value-of select="CdtrAcct/Id/Othr/Id"/> </AccountId>
                            <Currency> <xsl:value-of select="CdtrAcct/Ccy"/> </Currency>
                        </Creditor>

                    </xsl:for-each>
                    <Debtor>
                        <AccountName> <xsl:value-of select="PmtInf/Dbtr/Nm"/> </AccountName>
                        <AccountId> <xsl:value-of select="PmtInf/DbtrAcct/Id/Othr/Id"/> </AccountId>
                        <Currency> <xsl:value-of select="PmtInf/DbtrAcct/Ccy"/> </Currency>
                        <OrgId> <xsl:value-of select="PmtInf/Dbtr/Id/OrgId/Othr/Id"/> </OrgId>
                    </Debtor>
                </PaymentInformation>
            </PaymentXmlData>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
