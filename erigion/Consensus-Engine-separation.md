## Validation of headers

![](https://github.com/ledgerwatch/erigon/blob/devel/docs/Consensus-Engine-Page-1.png)

## Validation of uncles (EtHash)

To the best of our knowledge, EtHash is the only algorithm where this functionality is required. But something similar may come up with DAG-based algorithms, where headers have more than just a parent, but also alternative ancestors. In that case, a lot of the interface may need to be generalised accordingly.

![](https://github.com/ledgerwatch/erigon/blob/devel/docs/Consensus-Engine-Page-4.png)

## Use of smart contract state for Consensus Engine

![](https://github.com/ledgerwatch/erigon/blob/devel/docs/Consensus-Engine-Page-2.png)

In algorithms like AuRa (Authority Round), verification of headers requires access to the state of smart contracts (where, for example, set of validators is stored), as well as emitted events (requests for inducting new validators). In order to accommodate this, we would introduce another message type from Consensus to Core.

![](https://github.com/ledgerwatch/erigon/blob/devel/docs/Consensus-Engine-Page-6.png)

## Solution for Staged sync

![](https://github.com/ledgerwatch/erigon/blob/devel/docs/Consensus-Engine-Page-3.png)

## Fork choice rule

For choice rule can be thought of a partial order relationship among the set of possible headers. Being partial order, fork choice rule is:

1. reflexive or irreflexive, depending on whether non-strict or strict definition is required. header `A` is either better than itself (non-strict, `<=`), or not better than itself (strict, `<`)
2. anti-symmetric. if `A` better than `B`, then `B` is worse than `A`)
3. transitive. if `A` better than `B` and `B` is better than `

Core is asking the Consensus Engine to infer the relation between given headers, and perform topological sort.

![](https://github.com/ledgerwatch/erigon/blob/devel/docs/Consensus-Engine-Page-5.png)

## Finalisation Code

When the verification results in `Valid` message from Consensus Engine to the Core, an extra field `Finalisation Code` is attached. This code is expresses in an extension of EVM, which is currently called TEVM, and it needs to be run at the end of processing of the corresponding block (where there is access to the state etc.). For example, for assigning mining reward, the following very generic code can be finalisation code can be sent:

```
PUSH32 <block reward>
COINBASE
ADDBALANCE
```

Note that EVM does not have `ADDBALANCE` opcode, this would be part of TEVM extension, and this particular opcode would be run in a privileged mode only (meaning that only system parts like Consensus Engine and Transpiler may emit this code, but no user code with such code will be able to run). Similarly, EVM lacks introspection of uncles attached to the block. With extension allowing for that, the code above can be made to also add uncle rewards. In other consensus algorithms, the Finalisation code can be made to issue POS rewards, do slashing, etc., and it may either be very specific for every header or block, or parametrised via using extension opcodes in TEVM.

## Notes from Dragan

Hello, for consensus engine in OE there is main abstraction that all Engines implement: https://github.com/openethereum/openethereum/blob/32d8b5487a6fc12c8295ebf9833c74857f5e7354/crates/ethcore/src/engines/mod.rs#L304 And few months ago I wanted to see where and who is using engine and while doing that came up with this document: https://docs.google.com/spreadsheets/d/1gzkq_m7rHZKP7tPDyJiBykDjPgV8BCgiEwSZ35Zx3ho/edit?usp=sharing maybe it can be useful for AuRa

## Implementation

1. There are a few AuRa-based chain: Kovan, Sokol, xDAI (POS DAO)
2. We take Sokol as the first example, because Kovan has WASM contracts.
3. Re-add `eth/65` to `--download.v2` (messages with RequestID)
4. Connect to Sokol network from TG with `--download.v2`
5. Add `seal` into the header structure to parse header. Once it is done, we will see the signatures.
6. Implement finality rule using signatures.
7. ....
