By state sync here we mean the mechanism for nodes in a p2p network, to acquire the latest state of the system from other nodes, without going through all the state transitions that existed from the beginning of the network. This mechanism is especially important for Validity Proof systems like StarkNet, because one of their scalability advantages is the ability to prove the correctness of a large number of state transitions without the verifier needing to replay all those transitions.

The main challenge and dilemma of the state sync is as follows. On one hand, in order to replicate something most efficiently, the replicated thing needs to be as "static" as possible (meaning it does not change very frequently). The best efficiency is achieved when the replicated thing never (or almost never) changes. On the other hand, a good balance between scalability and usability is achieved when the state of the system changes reasonably quickly.

To address this dilemma, we suggest a decomposition of the state into multiple parts. These parts are on the spectrum - some being very static and therefore more efficient to replicate, others - dynamic (and harder to replicate) but relatively small. We can achieve this decomposition because we assume that only a small portion of the state actually changes at every update, and there is always large portion which does not.

![](https://github.com/ledgerwatch/erigon/blob/devel/docs/state_composition.png)

## Building blocks

The multiple parts the state is split into are of two types:

-   Big and static enough (meaning that they do not get obsolete very quickly) are worth sharing, so they are stored (and accessed) as static files
-   Small and quickly obsolete (by being aggregated into bigger parts), so not worth sharing, so they are stored (and accessed) as records in a database

First, we turn our attention to the static files. As these files represent the changes that occur in the state within a certain range of blocks, we can imagine each file containing a collection of key-value pairs. There are 3 main requirement to how this collection needs to be organised:

1. Efficient random access - given a key, we need to find a value
2. Efficient iteration in sorted order - when we merge two collections (from adjacent block ranges) into a bigger one, efficient iteration in sorted order is required
3. File size as small as possible for efficient dissemination over the Content Deliver Networks

We can get a reasonably satisfactory solution by using trees (like B+ trees), but we believe much better results can be achieved with some extra engineering.

To satisfy the requirement (2) for interaction in sorted order, we will first place key-value pairs one after another in the order of sorted keys, for example, encoding them with a prefix length encoding:

```
[key1_len] key1
[value1_len] value1
[key2_len] key2
[value2_len] value2
...
[keyN_len] keyN
[valueN_len] valueN
```

We can make two observations from here. Firstly, since keys and values are of variable length, we would need an extra "index" to point us to a starting position of a required key. Secondly, keys normally contain a lot of common prefixes, and repeating them in a collection would hurt the requirement (3) for the file size.

To address the second observation first, we can try to compress keys and values by using a common dictionary. A "general purpose" compression approach, when you take an uncompressed file, and produced its compressed version, is not applicable here, because we do not want to decompress the entire file just to access a single key-value pair. Our compression scheme needs to compress and decompress key-value pairs individually.

To address the first observation, we can combine in the "index" the requirement to point to starting position of a required key, with the requirement of finding the position in the index corresponding to a key, efficiently (more efficiently than in a case of a tree). Here we will be utilising a minimal perfect hash function, which is a function that would (only for the set of keys in that given file), compute the position in the index. It is called "minimal perfect" hash function because it does not have collisions and is therefore a bijection.

![](https://github.com/ledgerwatch/erigon/blob/devel/docs/regular-hash.png)

Above there is a schematic representation of a regular hash table, where the size of the table is usually larger than the number of keys, and occasional collisions need to be dealt with. If there is a guarantee that there is no collision, we have perfect hash function:

![](https://github.com/ledgerwatch/erigon/blob/devel/docs/perfect-hash.png)

And finally, if the size of the table is the same as the number of keys, we have minimal perfect hash table:

![](https://github.com/ledgerwatch/erigon/blob/devel/docs/minimal-perfect.png)

For the minimal perfect hashing we use implementation in Golang that has been adopted from the implementation described in this paper: [Recsplit: Minimal perfect hashing via recursive splitting](https://arxiv.org/pdf/1910.06416.pdf)

The implementation is currently here: https://github.com/ledgerwatch/erigon-lib/blob/main/recsplit/recsplit.go

## Some data from compression (state and transactions)

Data for main net block 13370374, produced Oct-07-2021 07:06:05 AM +UTC

State was extracted from the database and stored into 3 files:

1. Accounts. Format - length prefixed (using Golang `binary.Varint` representation) address (20 bytes), followed by length prefixed (`binary.Varint`) serialisation of the account as in `types/core/accounts/Account.EncodeForStorage`).
2. Contract storage. Format - length prefixed address + location (20 + 32 = 52), followed by length prefixed value (without leading zeroes). Incarnations are removed from the keys, also storage belonging to self-destructed contracts is not kept in the file.
3. Contract code. Format - length prefix address (20 bytes), followed by length prefixed byte code. No deduplication.

Transactions (largest components in the block bodies) were grouped in the files 500k blocks in each. Format - length prefixed transaction payloads.

The following table presents the results of experiments with the code in `cmd/hack/hack.go` (currently in `compress-3` branch). All compressed files were fully decompressed to evaluate the average access time, and to ensure compression is lossless.

| File | Size, GiB | Compressed size, GiB | `(size-compressed_size)/size`, % | Average access time per record, microseconds |
| --- | --- | --- | --- | --- |
| Accounts | 4.6 | 4.4 | 4.3 | 0.576 |
| Storage | 33 | 22 | 33.3 | 0.878 |
| Code | 16 | 2.7 | 83.1 | 5.3 |
| Txs 12.5-13m | 27 | 13 | 51.8 | 1.99 |
| Txs 12-12.5m | 23 | 13 | 43.4 | 1.98 |
| Txs 11.5-12m | 21 | 11 | 47.6 | 1.98 |
| Txs 11-11.5m | 18 | 10 | 44.4 | 2.03 |
| Txs 10.5-11m | 18 | 10 | 44.4 | 1.98 |
| Txs 10-10.5m | 14 | 8.2 | 41.4 | 1.64 |
| Txs 9.5-10m | 11 | 6.4 | 41.8 | 1.64 |
| Txs 9-9.5m | 10 | 5.9 | 41 | 1.88 |
| Txs 8.5-9m | 9.7 | 5.7 | 41.2 | 1.56 |
| Txs 8-8.5m | 10 | 6.1 | 39 | 1.68 |
| Txs 7.5-8m | 10 | 6.3 | 37 | 1.72 |
| Txs 7-7.5m | 8.9 | 5.4 | 39.3 | 1.75 |
| Txs 6.5-7m | 8 | 5 | 37.5 | 1.65 |
| Txs 6-6.5m | 10 | 6.3 | 37 | 1.88 |
| Txs 5.5-6m | 11 | 7 | 36.3 | 1.72 |
| Txs 5-5.5m | 9.4 | 6.1 | 35.1 | 1.49 |
| Txs 4.5-5m | 11 | 7.2 | 34.5 | 1.37 |
| Txs 4-4.5m | 6.5 | 4.3 | 33.8 | 1.45 |
| Txs 3.5-4m | 2.3 | 1.5 | 34.7 | 1.09 |
| Txs 3-3.5m | 669 MiB | 510 MiB | 23.7 | 0.816 |
| Txs 2.5-3m | 467 MiB | 344 MiB | 26.3 | 0.718 |
| Txs 2-2.5m | 610 MiB | 376 MiB | 38.3 | 0.964 |
| Txs 1.5-2m | 479 MiB | 356 MiB | 25.6 | 0.677 |
| Txs 1-1.5m | 380 MiB | 267 MiB | 29.7 | 0.725 |
| Txs 0.5-1m | 225 MiB | 134 MiB | 40.4 | 1.2 |
| Txs 0-0.5m | 73 MiB | 49 MiB | 32.8 | 0.792 |
| Txs total | 241.7 | 140.4 | 41.9 |  |
