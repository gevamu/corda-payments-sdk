<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pain="urn:iso:std:iso:20022:tech:xsd:pain.001.001.09"
                version="1.0">
    <xsl:output method="xml" encoding="UTF-8" indent="no"/>

    <xsl:template match="/PaymentRequest">
        <pain:CstmrCdtTrfInitn>
            <pain:GrpHdr>
                <pain:MsgId><xsl:value-of select="./MsgId"/></pain:MsgId>
                <pain:CreDtTm><xsl:value-of select="./CreDtTm"/></pain:CreDtTm>
                <pain:NbOfTxs>1</pain:NbOfTxs>
                <pain:InitgPty>
                    <pain:Nm><xsl:value-of select="./Debtor/AccountName"/></pain:Nm>
                </pain:InitgPty>
            </pain:GrpHdr>
            <pain:PmtInf>
                <pain:PmtInfId><xsl:value-of select="./PmtInfId"/></pain:PmtInfId>
                <pain:PmtMtd>TRF</pain:PmtMtd>
                <pain:ReqdExctnDt>
                    <pain:Dt><xsl:value-of select="./ReqdExctnDt"/></pain:Dt>
                </pain:ReqdExctnDt>
                <pain:Dbtr>
                    <pain:Nm><xsl:value-of select="./Debtor/AccountName"/></pain:Nm>
                    <pain:PstlAdr>
                        <pain:Ctry><xsl:value-of select="./Debtor/Country"/></pain:Ctry>
                    </pain:PstlAdr>
                    <pain:Id>
                        <pain:OrgId>
                            <pain:Othr>
                                <pain:Id><xsl:value-of select="./ParticipantId"/></pain:Id>
                            </pain:Othr>
                        </pain:OrgId>
                    </pain:Id>
                </pain:Dbtr>
                <pain:DbtrAcct>
                    <pain:Id>
                        <pain:Othr>
                            <pain:Id><xsl:value-of select="./Debtor/Account"/></pain:Id>
                        </pain:Othr>
                    </pain:Id>
                    <pain:Ccy><xsl:value-of select="./Debtor/Currency"/></pain:Ccy>
                    <pain:Nm><xsl:value-of select="./Debtor/AccountName"/></pain:Nm>
                </pain:DbtrAcct>
                <pain:DbtrAgt>
                    <pain:FinInstnId>
                        <pain:BICFI><xsl:value-of select="./Debtor/Bic"/></pain:BICFI>
                    </pain:FinInstnId>
                </pain:DbtrAgt>
                <pain:CdtTrfTxInf>
                    <pain:PmtId>
                        <pain:InstrId><xsl:value-of select="./InstrId"/></pain:InstrId>
                        <pain:EndToEndId><xsl:value-of select="./EndToEndId"/></pain:EndToEndId>
                    </pain:PmtId>
                    <pain:Amt>
                        <pain:InstdAmt>
                            <xsl:attribute name="Ccy"><xsl:value-of select="./Creditor/Currency"/></xsl:attribute>
                            <xsl:value-of select="./Amount"/>
                        </pain:InstdAmt>
                    </pain:Amt>
                    <pain:CdtrAgt>
                        <pain:FinInstnId>
                            <pain:BICFI><xsl:value-of select="./Creditor/Bic"/></pain:BICFI>
                        </pain:FinInstnId>
                    </pain:CdtrAgt>
                    <pain:Cdtr>
                        <pain:Nm><xsl:value-of select="./Creditor/AccountName"/></pain:Nm>
                        <pain:PstlAdr>
                            <pain:Ctry><xsl:value-of select="./Creditor/Country"/></pain:Ctry>
                        </pain:PstlAdr>
                    </pain:Cdtr>
                    <pain:CdtrAcct>
                        <pain:Id>
                            <pain:Othr>
                                <pain:Id><xsl:value-of select="./Creditor/Account"/></pain:Id>
                            </pain:Othr>
                        </pain:Id>
                        <pain:Ccy><xsl:value-of select="./Creditor/Currency"/></pain:Ccy>
                        <pain:Nm><xsl:value-of select="./Creditor/AccountName"/></pain:Nm>
                    </pain:CdtrAcct>
                    <pain:InstrForCdtrAgt>
                        <pain:InstrInf>ACC/SERVICE TRADE</pain:InstrInf>
                    </pain:InstrForCdtrAgt>
                    <pain:Purp>
                        <pain:Cd>CGOD</pain:Cd>
                    </pain:Purp>
                    <pain:RmtInf>
                        <pain:Ustrd>2037123 IT test</pain:Ustrd>
                    </pain:RmtInf>
                </pain:CdtTrfTxInf>
            </pain:PmtInf>
        </pain:CstmrCdtTrfInitn>
    </xsl:template>

</xsl:stylesheet>
