<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:iso="urn:iso:std:iso:20022:tech:xsd:pain.001.001.09">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

    <xsl:template match="/">
        <xsl:for-each select="iso:CstmrCdtTrfInitn">
            <PaymentXmlData>
                <Time> <xsl:value-of select="//iso:GrpHdr/iso:CreDtTm"/> </Time>
                <PaymentInformation>
                    <xsl:for-each select="//iso:PmtInf/iso:CdtTrfTxInf">
                        <CreditTransferTransactionInformation>
                            <PaymentIdentification>
                                <EndToEndId> <xsl:value-of select="//iso:PmtId/iso:EndToEndId"/> </EndToEndId>
                            </PaymentIdentification>

                            <Amount>
                                <InstdAmt> <xsl:value-of select="//iso:Amt/iso:InstdAmt"/> </InstdAmt>
                                <Currency> <xsl:value-of select="//iso:Amt/iso:InstdAmt/@Ccy"/> </Currency>
                            </Amount>

                            <Creditor>
                                <Name> <xsl:value-of select="//iso:Cdtr/iso:Nm"/> </Name>
                            </Creditor>

                            <CreditorAccout>
                                <Id> <xsl:value-of select="//iso:CdtrAcct/iso:Id/iso:Othr/iso:Id"/> </Id>
                                <Currency> <xsl:value-of select="//iso:CdtrAcct/iso:Ccy"/> </Currency>
                            </CreditorAccout>
                        </CreditTransferTransactionInformation>
                    </xsl:for-each>
                    <Debtor>
                        <Name> <xsl:value-of select="//iso:PmtInf/iso:Dbtr/iso:Nm"/> </Name>
                        <OrgId> <xsl:value-of select="//iso:PmtInf/iso:Dbtr/iso:Id/iso:OrgId/iso:Othr/iso:Id"/> </OrgId>
                    </Debtor>
                    <DebtorAccount>
                        <Id> <xsl:value-of select="//iso:PmtInf/iso:DbtrAcct/iso:Id/iso:Othr/iso:Id"/> </Id>
                        <Currency> <xsl:value-of select="//iso:PmtInf/iso:DbtrAcct/iso:Ccy"/> </Currency>
                    </DebtorAccount>
                </PaymentInformation>
            </PaymentXmlData>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
