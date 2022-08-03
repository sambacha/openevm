We do not define a specific deadline for transitioning from **Alpha** to **Beta** stage. Instead, we define the criteria that should help us decide when turbo-geth is ready for **Beta**. Here is the list of things that need implemented for these criteria to be met.

## Mining

The challenge of implementing efficient mining support in turbo-geth is the fact that there is only one "canonical" state at any given time. Mining, however, requires production of "speculative" blocks, and then "speculative" state, in order to compute the state root hash for the header. Current idea for the "speculative" state is an in-memory cache that can be "cloned". By "cloning", we mean creating a lazy-shallow copy of the cache such that the changes to the cloned state do not affect the canonical state. However, it turns out that the current data model of `HashedState` and `IntermediateHashes` is not well suited for the maintenance of such cache. The work is on-going to correct the data model and implement the clone-able state cache, and subsequently, the mining functionality.

## Simplified downloading of block headers and block bodies

Currently downloading block headers and block bodies are stage 1 and stage 3 of staged sync, respectively. These stages look and feel quite different from the other stages, because they were created on the foundations of the header/body/receipt/state downloading code inherited from go-ethereum. This code does much more than turbo-geth's staged sync requires, and can (and should) be replaced with a simplified version. A working proof-of-concept of this simplified version has been created, and now is the time for tests and documentation.

## Consensus Engine component

One may have heard about the concept of "pluggable consensus", meaning that it should be easy to switch from Proof Of Work to Proof of Authority, and also from one variant of POW to another one, and from one variant of POA to another one. In practice, pluggable consensus was some implementation with interfaces, but still always running in the same process, and often deeply intertwined with the rest of the code. We have taken steps to design the interface that would allow running consensus engine in the separate process. With such interface, it should be possible to run it in the same process, but the existence of the interface makes it much more straightforward to keep consensus engine properly separated. We have proof-of-concept implementation that works for EtHash POW and Clique POA, now it is time for integration, tests, and documentation.

## Switch from LMDB to MDBX

Erigon started off with the BoltDB database backend, then adding the support for BadgerDB, and then eventually migrating exclusively to LMDB. At some point we have encountered stability issues that were caused by our usage of LMDB that was not envisaged by the creators. We have since then been looking at a well-supported derivative of LMDB, called MDBX, and hoping to use their stability improvement, and potentially working more together in the future. The integration of MDBX is done, now it is time for more testing and documentation.

Benefits of transitioning from LMDB to MDBX:

1. Database file growth "geometry" works properly. This is important especially on Windows. In LMDB, one has to specify the memory map size once in advance (currently we use 2Tb by default), and if the database file grows over that limit, one has to restart the process. On Windows, setting memory map size to 2Tb makes database file 2Tb large on the onset, which is not very convenient. With MDBX, memory map size is increased in 2Gb increments. This means occasional remapping, but results in a better user experience.
2. MDBX has more strict checks on concurrent use of the transaction handles, as well as overlap read and write transaction within the same thread of execution. This allowed us to find some non-obvious bugs and make behaviour more predictable.
3. Over the period of more than 5 years (since it split from LMDB), MDBX accumulated a lot of safety fixes and heisenbug fixes that are still present in LMDB to the best of our knowledge. Some of them we have discovered during our testing, and MDBX maintainer took them seriously and worked on the fixes promptly.
4. When it comes to databases that constantly modify data, they generate quite a lot of reclaimable space (also known as "freelist" in LMDB terminology). We had to patch LMDB to fix most serious drawbacks when working with reclaimable space (analysis here: https://github.com/ledgerwatch/erigon/wiki/LMDB-freelist-illustrated-guide). MDBX takes special care of efficient handling of reclaimable space and so far no patches were required.
5. According to our tests, MDBX performs slightly better on our workloads.
6. MDBX exposes more internal telemetry - more metrics of what happening inside DB. And we have them in Grafana - to make better decisions on app design. For example, after complete transition to MDBX (removing LMDB support) we will implement "commit half-full transactions" strategy to avoid spill/unspill disk touches. This will simplify our code further without affecting performance.
7. MDBX has support for "Exclusive open" mode - we using it for DB migrations, to prevent any other reader from accessing the database while DB migration is in progress.
