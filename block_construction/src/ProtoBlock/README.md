# Block Construction: Proto Block

A proto-block is a block that has been executed but has not been sealed.
The header is missing the nonce and mixhash, and can still accept extra data.

Proto-blocks are produced when transactions are executed, and can be turned into full valid blocks.

A **block header** that has not finished being sealed.

**toHeader**: Seals the header into a block header

**proto-block body**: is the representation of the intermediate form of a block body before being sealed.


```kotlin
/* source: https://github.com/apache/incubator-tuweni/blob/main/eth-blockprocessor/src/main/kotlin/org/apache/tuweni/blockprocessor/ProtoBlock.kt */

/**
 * A block header that has not finished being sealed.
 */
data class SealableHeader(
  val parentHash: Hash,
  val stateRoot: Hash,
  val transactionsRoot: Hash,
  val receiptsRoot: Hash,
  val logsBloom: Bytes,
  val number: UInt256,
  val gasLimit: Gas,
  val gasUsed: Gas,
) {

  /**
   * Seals the header into a block header
   */
  fun toHeader(
    ommersHash: Hash,
    coinbase: Address,
    difficulty: UInt256,
    timestamp: Instant,
    extraData: Bytes,
    mixHash: Hash,
    nonce: UInt64,
  ): BlockHeader {
    return BlockHeader(
      parentHash,
      ommersHash,
      coinbase,
      stateRoot,
      transactionsRoot,
      receiptsRoot,
      logsBloom,
      difficulty,
      number,
      gasLimit,
      gasUsed,
      timestamp,
      extraData,
      mixHash,
      nonce
    )
  }
}

/**
 * A proto-block body is the representation of the intermediate form of a block body before being sealed.
 */
data class ProtoBlockBody(val transactions: List<Transaction>) {
  /**
   * Transforms the proto-block body into a valid block body by adding ommers.
   */
  fun toBlockBody(ommers: List<BlockHeader>): BlockBody {
    return BlockBody(transactions, ommers)
  }
}

/**
 * A proto-block is a block that has been executed but has not been sealed.
 * The header is missing the nonce and mixhash, and can still accept extra data.
 *
 * Proto-blocks are produced when transactions are executed, and can be turned into full valid blocks.
 */
class ProtoBlock(
  val header: SealableHeader,
  val body: ProtoBlockBody,
  val transactionReceipts: List<TransactionReceipt>,
  val stateChanges: TransientStateRepository
) {

  fun toBlock(
    ommers: List<BlockHeader>,
    coinbase: Address,
    difficulty: UInt256,
    timestamp: Instant,
    extraData: Bytes,
    mixHash: Hash,
    nonce: UInt64,
  ): Block {
    val ommersHash = Hash.hash(RLP.encodeList { writer -> ommers.forEach { writer.writeValue(it.hash) } })
    return Block(
      header.toHeader(ommersHash, coinbase, difficulty, timestamp, extraData, mixHash, nonce),
      body.toBlockBody(ommers)
    )
  }
}
```
