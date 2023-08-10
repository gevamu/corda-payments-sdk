# corda-payments-sdk

This repository contains Gevamu Payments Solution SDK source code and sample full-stack application.

## Introduction

[Gevamu (from Sinhala: ගෙවමු – *Let’s pay*) Payments Solution](https://gevamu.com/), developed by [Exactpro](https://exactpro.com/) based on [R3 Corda](https://www.r3.com/products/corda/), is aimed at fund distribution between a payer (the Participant) and a Payment Service Provider (PSP).

### Process description

A payment instruction submitted by the Participant as a document formatted according to a payment standard (e.g. ISO 20022) is consumed by the Payments CorDapp and passed through the Gevamu Gateway implemented as another CorDapp installed on the Corda node within the same business network.

The Gevamu Gateway acts as an authorized end-point connected to the payment gateway on the side of the PSP and transfers the payment instruction received from the Participant on-chain to the PSP located off-chain. The Gevamu Payments solution manages the payment flow. As part of the flow, it creates Corda states and updates them based on responses received by the Payment Gateway from the PSP. Payment status updates are communicated back to the Participant.

## Documention

Please follow the documentation at [gevamu.github.io](https://gevamu.github.io)!
