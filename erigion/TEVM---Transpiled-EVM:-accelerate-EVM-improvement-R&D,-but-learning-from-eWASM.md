# TEVM

EVM bytecode may be transpiled into a different representation for more efficient execution and for other benefits. Perhaps the most important benefit is related to the change process - it is very hard to change EVM gradually, with every step being relatively small and backwards compatible at the same time.

Transpilation of EVM into Web Assembly (for performance reasons) was the idea behind starting eWASM (ethereum Web Assembly) project. Lessons need to be learnt from this. For example, are these statements true?:

1. One EVM instruction gets translated into many Web Assembly instructions, thus creating additional overhead.
2. Executing many Web Assembly instructions takes longer than executing a corresponding single EVM instruction. Having higher-level instruction may be inconvenient (as referred later), but it does have a benefit of amortising the cost of the interpreter loop.
3. Web Assembly code is harder to meter efficiently than EVM bytecode, again due to the finer granularity of operations. Because of this finer granularity, the relative overhead of metering for Web Assembly code can be much higher than for EVM bytecode.
4. Problems above are not unsolveable

EVM, being a quite low-level virtual machine, has certain features that would belong to a high-level one, and these features make analysis of EVM code and optimisations harder than they should be. TEVM is an attempt to design another Virtual Machine, in such a way that EVM bytecode can be transpiled into TEVM code. Then, the TEVM can be executed instead of original bytecode, with the same result (perhaps up to some caveats). As mentioned above, splitting higher level instructions into lower-level ones needs to be done with a great care, because overhead of interpreting and metering increases with the increased granularity of operations.

## Features of EVM to be removed in TEVM

Here is a sample (not complete) list of features that EVM has, but TEVM would not have, and some ideas of how to transpile these features into TEVM.

### Path-dependent I/O (state access)

EVM is lacking intra-frame communication facilities, like Transient Storage (https://eips.ethereum.org/EIPS/eip-1153) that would be useful to implement things like mutexes. Instead, the contract storage is used for such things, and the relatively high cost of `SSTORE` and `SLOAD` operations led to the situation where the gas costs of these operations are highly variable dependening on what EVM was doing before (assuming some form of caching happening). This situation resembles "invisible" CPU caches creates a lot of drawback for various analysis and optimisations. In TEVM, there would instead be state accessing operations that have constant gas cost, and never assume any caching. In addition, there would be instructions for associative memory (more complex than EIP-1153, of course, to allow for namespaces and write protection where required) that can be used for caching.

## Pre-compiles

Precompiles were introduced as short-cuts mostly for compute-intensive cryptographic operations. Although they do solve important problem, the solution is far from elegant. Current idea is inspired by the work done on EVM384 and its generalisation, EVMn (where n - length of the word), to implement cryptography in TEVM.

## Features that TEVM would have that EVM does not

### Operation immediates

to allow more optimal implementation of cryptography, as well as static jumps.

### Priviledged operations

(only available in the "system/kernel" mode), with the ability to perform state modifications that are forbidden in EVM, for example, changing the balances of the accounts. This may allow expressing things like mining reward in TEVM instead of hard-coding it into the consensus engine.

### Non-deterministic execution with "hints"

Similar to https://www.cairo-lang.org/docs/how_cairo_works/cairo_intro.html#non-deterministic-computation

This feature can take two forms. First form is useful for computations that are harder to perform than to verify. A good example of that is multiplicative inverse on finite fields, used in many cryptographic algorithms. It is relatively computationally intensive to calculate, but much less so to verify (you need to perform multiplication and check that the result is 1).

Second form is useful for re-executing transactions that have already been executed by a miner. The hints here may include the information on which branch of execution is taken when `JUMPI` opcode is encountered.

## Path forward

The most immediate use-case for TEVM is the separation of Consensus Engine. Here is the problem: if consensus engine is separated from the Core, we can image how such functions as `VerifyHeader` and `ChooseBestHeader`, or `SealHeader` would work. But there is another important function, let's call it `FinaliseBlock`. For EtHash, for example, this is where miner rewards get added. Since this needs to happen for every single block, and it is not clear how to make this work across the interface (because giving out mining reward requires write-access to the state). Currently the idea of the solution (not the easiest thing to do though) is to express "FinaliseBlock" as a piece of code. First thought was to express it in EVM. But EVM does not have instructions to simply `AddBalance` to an account. Using privileged TEVM code would help. Solving this use case does not require full functionality of EVM, but it might be an interesting experiment of replacing EVM interpreter with TEVM interpreter, and adding transpilation step at deploy time and at sync time for already deployed contract.

Once the first use case is solved, and necessary infrastruture for TEVM interpreter and EVM->TEVM transpiler is created, multiple further experiments can be started:

1. Converting existing and future (BLS curve) precompiles to TEVM (with extended word instructions) and tailoring TEVM architecture and instructions for optimal result.
2. Unrolling `SSTORE` and `SLOAD` instructions into TEVM code that uses new instructions for associative memory, some control flow. Also, to match the gas cost of the `SSTORE` and `SLOAD`, transpiler may need to emit gas balancing instructions (priviledged).
3. Turning static analysis that builds up Control Flow Graph into TEVM code that directly supports static jumps and branching via specialised instructions.

## On priviledged instructions

So far, couple of use cases for priviledged instructions in TEVM appeared. Firsly, adding balance to an account for emulating mining rewards. Secondly, gas balancing instructions that can be inserted into TEVM instruction set, to add or subtract to/from current gas counter to make sure the unrolling of `SLOAD`, `SSTORE` and precomplies match their EVM gas costs. But what are the privileged instructions? TEVM can run in "user" mode and "system" (priviledged) mode. Priviledged instructions can only be executed in the "system" mode. When does the TEVM work in the "system" (priviledged) mode? So far two cases make sense:

1. When executing TEVM code generated by the EVM->TEVM transpiler.
2. When executing TEVM code given by consensus engine to be added at the block finalisation (mining rewards).
3. When executing irregular state transitions (e.g. DAO hard fork).

## Case study - unrolling `SSTORE` and `SLOAD`

The goal here is to transpile `SSTORE` and `SLOAD` EVM instructions into sequence of `TEVM` instructions that perform equivalent actions, and cost the same amount of gas in all cases, but have property that each TEVM generated TEVM instruction has a fixed (path independent) gas cost. Doing so requires introducing these kind of instructions:

1. `CSTORE` - analog of `SSTORE`, but always costs 20k gas
2. `CLOAD` - analog of `SLOAD`, but always costs 2600 gas
3. `PTCSTORE <arena>` - `P`rivileged `T`ransient `C`ontract-scope `STORE` - takes 1 immediate (1 byte long?) which is id of the arena. Different arenas are completely separate from each other. Non-generated code is non-privileged, therefore it cannot overwrite or read the values written by this instruction. The value written is transient because it only exists within 1 transaction. It is contract-scoped because values written in this way from different contracts, are completely separate. This instruction reads the location and the value from the stack and stores it into transient associative memory under specified arena and scope of currently executing contract.
4. `PTCEXIST <arena>` takes location from the stack and pushes back 1 if the value in the transient associative memory for given arena and contract scope exists (has been written by `PTCSTORE` instruction). This instruction allows distinguishing zero from non-existent value, which `LOAD` instructions do not provide.
5. `PTCLOAD <arena>` takes location from the stack and pushes the value from the transient associative memory, or zero if there is no value.
6. `PADDGAS <amount>` privileged instruction to add specified amount of gas to the gas counter. Required to make sure gas cost of the transpiled fragment matches exactly the cost of `SSTORE` or `SLOAD`.
7. `PSUBGAS <amount>` privileged instruction to remove specified amount of gas from the gas counter. Similar to the previous instruction.

For simplicity, gas cost of privileged operations is set to 0, and can be figured out and changed once non-privileged version is introduced.

For unrolling `SSTORE` and `SLOAD`, the transient memory is used instead of the internal caching done by the EVM implementation.

I might post the concrete example of unrolling here, when I get around to it. Developing this idea further, the same mechanism can be used to unroll other opcodes, like `BALANCE` and `EXTCODEHASH` etc, that unfortunately joined the set of ocodes with path-dependent gas cost. At the logical end of this process we may see a lot of special path-dependent logic which currently resides "around" the EVM, to be moved into the transpiler.

## Case study - structured control flow via abstract interpretation

Here we can use the abstract interpretation code that exists in turbo-geth but not integrated into the actual workings of the sync or EVM. Currently, this static analysis is able to build Control Flow Graph successfully for 97% of deployed contracts. However, it is not yet able to determine how to generate code for structured control flow, i.e., where to insert subroutines and structures that look like `switch/case` statements. This requires some additional work. However, if this work is done, we can opportunistically add structured control flow instructions (EIP-615) into TEVM and transpile as many existing contracts as we can. Note that initially, the high algorithmic cost of transpilation may be offset by the lazy transpilation, because TEVM will still retain dynamic jump instructions for a while (until the "Endgame" described below).

## Potential endgame

If the development of TEVM, driven by use cases of modularity, optimisation, static analysis, goes well, it may reach the state where it is a virtual machine without pre-compiles, with fixed-cost instructions, and potentially some other nice things, like superior interpreter speed. At that point the question may arise about replacement of EVM with TEVM and Ethereum supporting TEVM natively, and then perhaps sun-setting EVM. Given the current change process in Ethereum, it is unlikely that serious improvements can make it into EVM in reasonable time. Instead, we can expect more and more modifications trying to balance backwards compatibility and some pressing need from application developers, and due to this balance being sub-optimal for the EVM architecture. Given the above, TEVM might be a much more realistic way to actually make real improvement in EVM at reasonable speed.
