# Alpha

Erigon (formerly known as turbo-geth) has been in development since Dec 2017, and we started alpha releases on 30th of July 2020. Alpha marked the stage of development where we invited the users to use the software, and provide us with feedback. We also knew that there were a lot of missing pieces of functionality, and that there would still be a lot of changes to the data model. We are very grateful to all the people who decided to give Erigon a try, and shared their successes and failures with us.

# Criteria for transitioning from Alpha to Beta

At some point through the alpha program, we set out criteria for our transition to beta stage, where active development of the first version will mostly stop, and only bug fixes and critical features would be added. These were the criteria (https://github.com/ledgerwatch/erigon/wiki/Criteria-for-transitioning-from-Alpha-to-Beta):

1. Separable p2p sentry component and the new algorithm for downloading block headers and block bodies, more aligned with the "staged sync" architecture than pre-existing algorithms.
2. Migration of the backend database from LMDB to MDBX, for more flexible API and better diagnostics.
3. Support for mining/block composition.
4. Separable consensus engine.

So far, 3 out of 4 criteria have been met. We will have to admit that the 4th criterion will not be met in the first version, and is moved to the version two. In the intervening time, Ethereum London hard fork brought about EIP-1559, which required changes in the transaction pool, so we prioritised separable transaction pool suitable for EIP-1559, above the separable consensus engine. We still think that separable consensus engine is very important for further growth of the ecosystem, but decided that it will not be in the version one. However, we are pleased to note that the separable transaction pool with the support of EIP-1559 is implemented and will be part of Erigon version one.

# Regression testing for JSON RPC API

Another requirement that we imposed on ourselves for the transition to beta version one was at least some mechanism for regression testing of the JSON RPC API, something that most users of Erigon rely on. We are pleased to announce that some initial ground work has been done and we have set up QA (Quality Assurance) server, which will become part of our toolbox for better release engineering. Current version can be seen on https://erigon.dev and the repository linked to it is https://github.com/ledgerwatch/rpctests. We will continue to build up the QA server as a part of Erigon beta one program.

# What is Beta program exactly?

Starting from the release which is currently planned for the 4th of November 2021 (which will be the first beta release), we will stop active development of Erigon version 1 and will only add:

-   bug fixes
-   missing features (mostly for RPC requests)
-   parameter changes for "skip analysis" optimisation
-   updates for pre-verified block hashes for mainnet and Ropsten

Importantly, we will not try to optimise code unless there is a serious performance issue, and focus instead on stability.

We will use `stable` branch (this is where we are currently performing alpha releases from) for Erigon 1 beta. Active development of Erigon 2 will continue on `devel` branch. At some point, we will decide to transition Erigon 2 into alpha stage, at which point we will invite users to give it a try again. Prior to alpha stage, we will not be providing data migration for Erigon 2 and that will allow us to make drastic changes to the data model as quickly as possible. We will create a stable branch for Erigon 2 alpha in the due course.

# What is planned for Erigon 2?

Initially, the separable consensus engine component, as an unfinished task from Erigon 1, has been added to the scope of Erigon 2. However, since then we have reviewed it and decided to remove this from the scope. The reasoning is as follows. The Merge will most likely lead to removal of the support for PoW and PoA algorithms from major Ethereum implementation. We expect the PoW testnets to be sun-set, followed by PoA testnets, and replaced by PoS testnet. Initially, Proof of Authority testnets were introduced to solve the problem of PoW testnet Ropsten being too vulnerable to frivolous mining attacks causing frequent disruptions. But if we have robust Proof Of Stake implementations available, there is not much point maintaining Proof Of Authority testnets any longer. They can be sun-set and replaced by PoS testnets. Therefore, we will stop working on consensus separation for now, and review this some time after The Merge.

Secondly, state sync. This is something we wanted to introduce in Erigon 1, but it turned out to be quite challenging without major redesign of the data model, and so was never finished. State sync is the mechanism for bootstrapping the node without requiring to replay all the blocks and transactions from Genesis (which is the only way to bootstrap Erigon version one). We believe that this is probably the most important drawback of Erigon 1. This work is also laying one of the "foundation stones" for our collaboration with Starknet on Fermion project. Some initial design documentation can be found on a Wiki page here: https://github.com/ledgerwatch/erigon/wiki/State-sync-design

Another important improvement for which we need a drastic rework of the data model is the increased granularity of our indices (from one item in the index for block to one item per transaction). We are still setting up computational experiments to see if this could be done without increasing the disk footprint. If this is possible (which is likely), performance of most trace queries and search queries can be improved a lot, especially for the part of the blockchain where the blocks are large. This should be beneficial for data intensive projects building directly on top of Erigon (e.g. Otterscan, TrueBlocks). Perhaps it will also allow us to have reasonable performance for re-generation of transaction receipts on the fly (this is already possible in Erigon 1, but is at least 10x slower than reading from stored receipts - stored receipts are quite large unfortunately).

We are already conducting experiments of reorganising the way Erigon 2 will store block bodies (transaction payloads mostly) outside of the main database. This is somewhat similar to "freezer" in Geth, but with larger files, making it more suitable for sharing via content delivery networks (like BitTorrent).

There might potentially be more things, but they might not fit into Erigon 2 scope, which is already quite big.

# What about The Merge?

Will Erigon 1 support The Merge? Probably. Once Erigon 1 beta program starts, we will develop support for The Merge in `devel`, as a part of Erigon 2. It is likely that we will need to backport it to Erigon 1 once it works well enough, to make Erigon 2 timeline more flexible. But it will depend on how well Erigon 2 research and development comes along.

# Is Erigon 1 safe to use (for example with Beacon chain validator)?

There are some users that successfully use Erigon with a Beacon chain validator. But there has been issues in the past, most of them introduced by new changes in the alpha version. Sometimes fixes were easy, sometimes it required a full resync (which is luckily not so burdensome compared to other archive node installations). The idea behind Erigon 1 beta is that over time we can be more confident that there are no serious issues. Beta does not mean it is very solid already, but it means it will start solidifying instead of being quite "liquid" in alpha. We will make a separate judgement on when to remove "beta", this will be based on our perception of how many issues are still happening. It will also depend on how well our QA process development is going.

Beta is also for us, developers of Erigon, to have a bit of breathing space and go "crazy" for a bit on the second version, without fear of breaking users's installations.

Also, with the beta is should be easier for you as a user to make your own judgement on whether this is "safe enough" for your use case, because we will not be "pulling rug" out of your system by putting lots of changes every week and risking a regression or database corruption.

# What about mining?

Nominally, mining works in Erigon 1 with the option `--mine`. However, we did not give it enough testing ourselves and we did not yet launch a mining test net as we planned. Therefore, we would like to invite miners who would like to test Erigon and give us feedback, to go ahead. Fixes to mining functionality will be considered important enough to be fixed in Erigon 1 beta.

Are there any advantages in using Erigon instead of other implementations for mining? We do not know yet for sure because we did not test it ourselves.
