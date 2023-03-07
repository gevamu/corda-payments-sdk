<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

    <xsl:template match="/">
        <xsl:for-each select="CstmrCdtTrfInitn">
            <PaymentXmlData>
                <Time> <xsl:value-of select="GrpHdr/CreDtTm"/> </Time>
                <PaymentInformation>
                    <xsl:for-each select="PmtInf/CdtTrfTxInf">
                        <CreditTransferTransactionInformation>
                            <PaymentIdentification>
                                <EndToEndId> <xsl:value-of select="PmtId/EndToEndId"/> </EndToEndId>
                            </PaymentIdentification>

                            <Amount>
                                <InstdAmt> <xsl:value-of select="Amt/InstdAmt"/> </InstdAmt>
                                <Currency> <xsl:value-of select="Amt/InstdAmt/@Ccy"/> </Currency>
                            </Amount>

                            <Creditor>
                                <Name> <xsl:value-of select="Cdtr/Nm"/> </Name>
                            </Creditor>

                            <CreditorAccout>
                                <Id> <xsl:value-of select="CdtrAcct/Id/Othr/Id"/> </Id>
                                <Currency> <xsl:value-of select="CdtrAcct/Ccy"/> </Currency>
                            </CreditorAccout>
                        </CreditTransferTransactionInformation>
                    </xsl:for-each>
                    <Debtor>
                        <Name> <xsl:value-of select="PmtInf/Dbtr/Nm"/> </Name>
                        <OrgId> <xsl:value-of select="PmtInf/Dbtr/Id/OrgId/Othr/Id"/> </OrgId>
                    </Debtor>
                    <DebtorAccount>
                        <Id> <xsl:value-of select="PmtInf/DbtrAcct/Id/Othr/Id"/> </Id>
                        <Currency> <xsl:value-of select="PmtInf/DbtrAcct/Ccy"/> </Currency>
                    </DebtorAccount>
                </PaymentInformation>
            </PaymentXmlData>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
