# How to run

After checking in the code from the desired branch, run the following commands:

```
make state
./build/bin/state erigon2 --datadir <your_datadir>
```

The directory referenced by the `--datadir` option needs to contain block headers, block bodies, and recovered senders downloaded and computed by a recent version of Erigon. All stages are not required, only first four: Headers, BlockHashes, Bodies, Senders.

The prototype starts replaying blocks and their transactions starting from genesis, and then block 1, block 2, and so on. Every 30 seconds it prints the progress, like so:

```
INFO[02-02|09:53:53.205] Progress                                 block=505475 blk/s=6174.274 state files=32 accounts="1.2 MiB" code="1.0 MiB" storage="937.1 KiB" commitment="2.3 MiB" total dat="5.2 MiB" total idx="273.4 KiB"
INFO[02-02|09:54:23.211] Progress                                 block=651875 blk/s=4879.058 state files=24 accounts="1.4 MiB" code="1.7 MiB" storage="2.1 MiB" commitment="3.2 MiB" total dat="7.9 MiB" total idx="500.3 KiB"
INFO[02-02|09:54:53.214] Progress                                 block=726875 blk/s=2499.738 state files=16 accounts="1.4 MiB" code="1.8 MiB" storage="2.6 MiB" commitment="3.0 MiB" total dat="8.3 MiB" total idx="560.2 KiB"
```

It is possible to interrupt the prototype by pressing `Ctrl-C` on the console, or sending `SIGTERM` (-15) signal to the process on Unix. When this interruption happens, informations similar to the following is printed:

```
^CINFO[01-16|20:28:21.926] Got interrupt, shutting down...
INFO[01-16|20:28:21.926] Got interrupt, shutting down...
INFO[01-16|20:28:21.926] interrupted, please wait for cleanup, next time start with --block 956723
```

This information helps resume the prototype from the point it was interrupted, instead of starting from the beginning, like so (in our example):

```
./build/bin/state erigon2 --datadir <your_datadir> --block 956723
```

The replaying will resume from where it was interrupted, like so:

```
./build/bin/state erigon2 --datadir ~/mainnet --block 956723
INFO[01-16|20:28:59.088] Processed                                blocks=957000
INFO[01-16|20:28:59.833] Processed                                blocks=958000
INFO[01-16|20:29:05.028] Processed                                blocks=959000
INFO[01-16|20:29:05.729] Processed                                blocks=960000
```

# Which files it creates and where

The prototypes creates and modifies files in two directories:

```
<your_datadir>\aggregator
<your_datadir>\statedb
```

Files in the `aggregator` directory are of the following three types:

1. Change file (extension `.chg`). These files are created for 4 groups of content: accounts, storage, code, and commitment, this can be recognised by the first part of their file names. Within each group of content, there are 3 possible "sequences": keys, before values, and after values. By default, only keys and after values are written. Each file corresponds to an interval of blocks up to 4096 blocks large, starting and ending blocks (exclusive) are part of the file names. Change files can be though of "Write Ahead Log" (WAL) files that contain the recent history of changes in the state. The combination of keys and after values is then used to aggregate the changes into data files described next. After aggregation, change files are removed. When enabled, the plan for before files is to be used for unwinding the state, as well as for creating the change history, though none of these two features are implemented yet.
2. Data files (extension `.dat`). These files, like change files, are created for 4 groups of content: accounts, storage, code, and commitment. They also correspond to an interval of blocks, but unlike change files, the interval of blocks can be larger than 15625 (5^6) blocks. In fact, currently, it can be 31250, 62500, 125000 and so on blocks. Initially, data files for 15625 block intervals are created by aggregating the content of the corresponding change files. Then, individual data files can be merged with one another to form larger and larger data files. The plan for data files is to be seeded via Content Delivery Networks.
3. Index files (extension `.idx`). Every index file corresponds to its data file, and has almost the same file name, apart from the extension. Index files contain the serialised representation of the minimal perfect hash table, offset table (with each key or value in the data file having and offset in that table), and optionally a compressed mapping for accessing data file as an array of items. Index files are not going to be shared via Content Delivery Network, but instead created locally. For prevention potential DOS vulnerabilities, there is a plan for each Erigon2 node to build ideal hash table part from a randomly generated seed, so that no specific seed can be potentially exploited.

Files in `statedb` directory contain MDBX database that is used for storing recent state (recent 90k + 15625 blocks). The recent state is getting pruned on each aggregation, and that keep the State DB size quite low.

## Deleting prototype files

When the prototype is launched without `--block` command line option, it removes all the files from the `aggregator` and `statedb` directories. This is to make iterating over changing data formats more convenient, but needs to be taken into account to not accidentally remove the results of a long run.

# Command line options

Currently, apart from `--block` option that allows resuming the prototype run after a graceful interruption, there are following additional options:

## Frequency of commitment calculation

Option `--commfreq` has default value 256. It means that instead of calculating commitment (currently the root hash of a Patricia Merkle Tree over the state) happens after blocks whose numbers are multiple of 256. Calculated commitment is compared to what is stored in the corresponding block header. Setting `--commfreq 1` means calculating and checking commitment after every block, which has the highest performance overhead, but provides more thorough checking of the commitment calculation algorithm and code (which is still experimental and is not used anywhere except for this prototype). Setting it higher values (maximum is 4096, because otherwise it would interfere with producing commitment data files) reduces the performance overhead, allowing the prototype to run faster. It comes at a cost of highest memory usage, because state modifications are kept in memory between subsequent computations of the commitment.

## Producing change sets

Option `--changesets` is designed to turn on the generation of change sets with the granularity of a single transaction (as opposed to the granularity of a block, as in Erigon beta). This is not yet functional, and subject to further active development and changes. Also, this setting has to enable writing the before value change files.

## Profiling the prototype

When option `--pprof` is used, the prototype starts up a Web server with standard Golang pprof capabilities. It prints out following:

```
INFO[01-16|21:49:04.253] Starting pprof server                    cpu="go tool pprof -lines -http=: http://127.0.0.1:6060/debug/pprof/profile?seconds=20" heap="go tool pprof -lines -http=: http://127.0.0.1:6060/debug/pprof/heap"
```

Accordingly, one can generate a CPU profile by running `go tool pprof` in a different console like so (to capture 2 minutes worth of CPU profiling:

```
% go tool pprof "http://127.0.0.1:6060/debug/pprof/profile?seconds=120"
Fetching profile over HTTP from http://127.0.0.1:6060/debug/pprof/profile?seconds=120
```

When profile comes back, one can use `png` command in the `pprof` console to generate a picture that may look like this: ![](https://github.com/ledgerwatch/erigon/blob/devel/docs/erigon2_profile.png)
