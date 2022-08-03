We often get asked why we opted for our current storage engine, [MDBX](https://github.com/erthink/libmdbx).

# Why not LevelDB / RocksDB?

Answer is pretty simple: no MVCC.

MVCC allows us to "stitch together" more complex data objects from more normalised form that is stored in the DB, without loss of consistency (if you do it all in a single read-only transaction). This is used quite a lot in the RPC daemon and simplifies the code a lot. You do not need to explicitly link all the data in the application-level code, you just trust that the database will give you consistent snapshot.

Other than that, Level and Rocks are not ACID. This makes them extremely brittle and prone to corruption on application crash or power failure. Given that node sync from genesis is not cheap or instantaneous, this is a non-starter for us.

# Why not BadgerDB?

BadgerDB, unlike Level or Rocks, does provide transactions. However, there is a next issue we run into: Badger is based on [Log-structured merge-tree](https://en.wikipedia.org/wiki/Log-structured_merge-tree).

Badger (and all LSM-based DBs) has background compaction. It's good for some projects and bad for others.

In Erigon we eliminated most of concurrency (goroutines) for many reasons (too many things happening at the same time). We found that modern SSD (and NVMe) are still pretty bad with concurrent writes - they are way better than HDD, but sequential read is still order of magnitude faster than random reads. Meaning 1 thread touching disk vs 2 threads touching disk - can show 10x degradation.

_How does this apply to us?_

**We removed parallel writes and moved to control all disk touches**. Now we don't really care about "how much WPS database can handle" because now we can fit all writes into 1 write transaction. Doesn't matter if it happens once per 10 minutes or once per 1 second - as long as it's not thousands of parallel WPS. In LMDB 1 write transaction is equal to 1 fsync syscall - all writes during transaction are happening in RAM.

LSM databases (Badger, LevelDB) are slower on average for random reads, and that read times are more volatile. B+tree is faster and more predictable for random reads.

# Why not BoltDB?

Unlike Badger, Bolt is a Go library, providing storage engine based on B+tree. It originally fit well, and we had BoltDB backend available until September 2020.

Bolt lacks certain advanced features that we found useful, like LMDB's sorted duplicates (DupSort). It allows to save space without resorting to compression by storing repetitive keys only once.

Bolt is not actively maintained anymore, [although there is an active fork by etcd team](https://github.com/etcd-io/bbolt). And finally, it is a Go library, precluding usage and binary compatibility with [Silkworm](https://github.com/torquem-ch/silkworm) and [Akula](https://github.com/rust-ethereum/akula).

For all these reasons we switched to LMDB.

# Why not LMDB?

[See this post.](https://github.com/ledgerwatch/erigon/wiki/Criteria-for-transitioning-from-Alpha-to-Beta#switch-from-lmdb-to-mdbx)
