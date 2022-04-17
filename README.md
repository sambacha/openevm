# Open Ethereum Virtual Machine

<small><i><a href='github.com/sambacha'>OpenEVM Kwowledgebase </a></i></small>


## [TOC]

+ [The Contract constructor function](#the-contract-constructor-function)
  + [Skeleton example of a very small constructor function](#skeleton-example-of-a-very-small-constructor-function)
  + [Compiling the smart contract](#compiling-the-smart-contract)
  + [Running the contract](#running-the-contract)
  * [Existing implementation](#existing-implementation)
- [Limitation](#limitation)
      - [Address layout](#address-layout)
      - [Limitations](#limitations)
      - [The function dispatcher (meta function)](#the-function-dispatcher--meta-function-)
      - [Moving the function dispatcher to front of the LLVM IR function list](#moving-the-function-dispatcher-to-front-of-the-llvm-ir-function-list)
- [Functionalities](#functionalities)
  * [Experimental support of landing pad](#experimental-support-of-landing-pad)
  * [Experimental support of simulating heap allocations](#experimental-support-of-simulating-heap-allocations)
  * [Constant table support](#constant-table-support)
  * [Metadata export](#metadata-export)
- [Optimizations](#optimizations)
  * [Support more than 16 local variables](#support-more-than-16-local-variables)
  * [Instruction scheduling](#instruction-scheduling)
  * [Improve EVM calling conventions](#improve-evm-calling-conventions)
  * [Re-materialization of constants](#re-materialization-of-constants)
  * [EVM LLVM Wiki](#evm-llvm-wiki)
    + [SHA3 (Keccak)](#sha3--keccak-)
    + [Address](#address)
    + [Balance](#balance)
    + [Origin](#origin)
    + [Caller](#caller)
    + [CallValue](#callvalue)
    + [CallDataLoad](#calldataload)
    + [CallDataSize](#calldatasize)
    + [CallDataCopy](#calldatacopy)
    + [CodeSize](#codesize)
    + [CodeCopy](#codecopy)
    + [GasPrice](#gasprice)
    + [ExtCodeSize](#extcodesize)
    + [ExtCodeCopy](#extcodecopy)
    + [ReturnDataSize](#returndatasize)
    + [ReturnDataCopy](#returndatacopy)
    + [BlockHash](#blockhash)
    + [Coinbase](#coinbase)
    + [Timestamp](#timestamp)
    + [Number](#number)
    + [Difficulty](#difficulty)
    + [GasLimit](#gaslimit)
    + [StorageLoad](#storageload)
    + [StorageStore](#storagestore)
    + [MemSize](#memsize)
    + [Gas](#gas)
    + [Log0](#log0)
    + [Log1](#log1)
    + [Log2](#log2)
    + [Log3](#log3)
    + [Log4](#log4)
    + [Create](#create)
    + [Create2](#create2)
    + [Call](#call)
    + [CallCode](#callcode)
    + [DelegateCall](#delegatecall)
    + [StaticCall](#staticcall)
    + [Return](#return)
    + [Revert](#revert)
    + [SelfDestruct](#selfdestruct)
    + [Frontend is expected to emit 256bit values LLVM IR](#frontend-is-expected-to-emit-256bit-values-llvm-ir)
    + [Frontend needs to generate compatible LLVM IR](#frontend-needs-to-generate-compatible-llvm-ir)
  * [Constructor](#constructor)
  * [Testing utilities](#testing-utilities)
  * [How to run testings](#how-to-run-testings)
  * [How to add new tests](#how-to-add-new-tests)
  * [TODO lists](#todo-lists)
      - [Frame Objects](#frame-objects)
    + [Frame Pointer (or Free Memory Pointer)](#frame-pointer--or-free-memory-pointer-)
    + [Memory stack](#memory-stack)
  * [Types of calls](#types-of-calls)
  * [Internal call conventions](#internal-call-conventions)
  * [Procedure of a subroutine call](#procedure-of-a-subroutine-call)
  * [[EIP2315](https://eips.ethereum.org/EIPS/eip-2315) Support: Subroutine calls](#-eip2315--https---eipsethereumorg-eips-eip-2315--support--subroutine-calls)
    + [To generate a call procedure](#to-generate-a-call-procedure)
    + [To generate the return](#to-generate-the-return)
  * [External calls](#external-calls)
  * [Contract Input Argument Types -- The Solidity convention](#contract-input-argument-types----the-solidity-convention)


# Introduction

The EVM **is a stack machine**. Instructions might use values on the stack as arguments, and push values onto the stack as results. Let’s consider the operation add.

Elements on the **stack** are 32-byte words, and all keys and values in **storage** are 32 bytes. There are over 100 opcodes, divided into categories delineated in multiples of 16.

The EVM is a security oriented virtual machine, designed to permit untrusted code to be executed by a global network of computers.

To do so securely, it imposes the following restrictions:

- Every computational step taken in a program's execution must be paid for up front, thereby preventing Denial-of-Service attacks.
- Programs may only interact with each other by transmitting a single arbitrary-length byte array; they do not have access to each other's state.
- Program execution is sandboxed; an EVM program may access and modify its own internal state and may trigger the execution of other EVM programs, but nothing else.
- Program execution is fully deterministic and produces identical state transitions for any conforming implementation beginning in an identical state.

**Ethereum ABI (Application Binary Interface)**: is the standard way to interact with contracts in the Ethereum ecosystem, both from outside the blockchain and for contract-to-contract interaction. It is a specification for how arguments and function calls should be encoded in the call-data

An EVM compiler doesn’t exactly optimize for bytecode size or speed or memory efficiency. Instead, it optimizes for gas usage, which is an layer of indirection that incentivizes the sort of calculation that the Ethereum blockchain can do efficiently.

Gas costs are set somewhat arbitrarily, and could well change in the future. As costs change, compilers would make different choices.

### EVM Implementations (Ethereum clients)

- go-ethereum (Go)
- Parity (Rust)
- EthereumJ (Java)

Some interesting implementations: 

- [eth-isabelle](https://github.com/pirapira/eth-isabelle): theorem provers
- [sputter](https://github.com/nervous-systems/sputter): implementation in Clojure


### Programming Languages that Compile into EVM

- Solidity
- [LLLL](https://github.com/mmalvarez/eth-isabelle/blob/master/example/LLLL.thy): An LLL-like compiler being implemented in Isabelle/HOL
- [HAseembly-evm](https://github.com/takenobu-hs/haskell-ethereum-assembly): An EVM assembly implemented as a Haskell DSL  
```haskell
    main :: IO ()
    main = putStrLn $ codegen prog1
    
    prog1 :: EvmAsm
    prog1 = do
        push1 0x10
        push1 0x20
        add
```
        
## Resources

additional refs:  

[EVM - Awesome List](https://github.com/ethereum/wiki/wiki/Ethereum-Virtual-Machine-(EVM)-Awesome-List)  

[CoinCulture - evm-tools](https://github.com/CoinCulture/evm-tools/blob/master/analysis/guide.md)

[Diving into the Ethereum EVM](https://blog.qtum.org/diving-into-the-ethereum-vm-6e8d5d2f3c30)



The project compiles like other LLVM projects. The target's name is `EVM`, but since it is not yet finalized, you have to specify `-DLLVM_EXPERIMENTAL_TARGETS_TO_BUILD=EVM` when you compile it.

In short,  you can use the following to build the backend:
```
git clone git@github.com:etclabscore/evm_llvm.git
cd evm_llvm
git checkout EVM
mkdir build && cd build
cmake -DLLVM_TARGETS_TO_BUILD=EVM -DLLVM_EXPERIMENTAL_TARGETS_TO_BUILD=EVM ..
make -j8
```



### The Contract constructor function
Because EVM's execution always start from the beginning of the code (`pc = 0`), there must be a way to handle more complicated contract behaviours. In EVM LLVM, we use a function to describe the function handling. It is called contract constructor function. To implement the function, developers are expected to respect the following contract constructor properties:
* The constructor should be the first function in the generated LLVM IR.
* The constructor should be named `solidity.main` or `main` (could change in the future). The backend recognizes these specific names and will generate different call codes.
* The constructor should not take any arguments.
* The constructor should initialize the function's `free memory pointer`, which is located at address `0x40`. The `free memory pointer` is like the usual frame pointer, used to calculate function frames and stack allocations. Because it is located at `0x40`, so you cannot initialize it to a smaller number.

### Skeleton example of a very small constructor function
Here is an illustration of the skeleton of a small smart contract:
```
declare i256 @llvm.evm.calldataload(i256)
declare void @llvm.evm.return(i256, i256)
declare void @llvm.evm.mstore(i256, i256)

define void @main() {
entry:
  call void @llvm.evm.mstore(i256 64, i256 128)
  %0 = call i256 @llvm.evm.calldataload(i256 0)
  %1 = call i256 @llvm.evm.calldataload(i256 32)
  %2 = call i256 @add(i256 %0, i256 %1)
  call void @llvm.evm.mstore(i256 0, i256 %2)
  call void @llvm.evm.return(i256 0, i256 32)
  unreachable
}

define i256 @add(i256, i256) #0 {
  %3 = alloca i256, align 4
  %4 = alloca i256, align 4
  store i256 %0, i256* %3, align 4
  store i256 %1, i256* %4, align 4
  %5 = load i256, i256* %3, align 4
  %6 = load i256, i256* %4, align 4
  %7 = add nsw i256 %5, %6
  ret i256 %7
}
```

**Usually, it is the frontend's responsibility to do the smart contract's plumbing, including the contract's constructor function. ** We need the language frontends to generate corresponding LLVM IR code.

This smart contract does the following things;
* Initialize the `free memory pointer` to 128
* parse the first two 32-byte inputs
* call the `@add` function and supply it with the two parsed arguments
* In the function `@add`, we simply add the two arguments, and return it
* In the `@main` function, return the retrieved value using `llvm.evm.return` intrinsic.

### Compiling the smart contract

Let's put the above smart contract code into a file named `test.ll`, and we use `llc` to generate EVM binary:
```
llc -mtriple=evm -filetype=obj test.ll -o test.o
```

### Running the contract
A generated `.o` file is in binary format. To see its content in hex, try to use `xxd`, for example:
```
xxd -p -cols 65536 test.o
```
The `xxd` will emit a hex string representation of the binary format. `xxd` will try to break the line if it is too long. Here we specify `-cols 65536` to avoid linebreaking. After calling `xxd`, you should see some output such as:
```
5b600135600080803561003d909192939091604051806108200152604051610840016040526004580192565b60405160209003516040529052602090f35b80826040519190915260206040510152019056
```

That is what we need to execute using an EVM engine. Let's try to do it using Geth's EVM. Remember that we need to supply two input arguments, so the command line should be like:
```
evm --input 1234567890123456789012345678901234567890123456789012345678901234 --code 5b600135600080803561003d909192939091604051806108200152604051610840016040526004580192565b60405160209003516040529052602090f35b80826040519190915260206040510152019056 run
```

`evm` will emit the result of the two added files:
```
0x468acf08a2468acf08a2468acf08a2468acf08a2468acf08a2468acf08a24634
```

EVM LLVM provides a way to emit program's metadata for various of purposes. For examples, a symbol table that records the jump destinations can be emitted along with the generated binary. 

Developers can use this utility to emit more program information. 

## Existing implementation

When compiling a contract, a file named `EVMMeta.txt` will be generated along with the binary code. The file contains the function symbol table in the compiled program, along with the offset of each function. The metadata file can be used for various purposes, such as debugging, manual linking, analysis, and so on.

To specify a custom metadata file name if you do not want to use the `EVMMeta.txt` filename, option `-evm_md_file` can be used.

# Limitation

Existing implementation of EVM metadata emitting is limited to `MachineCode` module/level, which means that if there are any transformations at a higher level such as in the IR level, it will not be shown in the result.



 Let's try to use a simple C file to test our compiler:
```sh
cat <<EOF > test.c
unsigned x;
int abc(unsigned a, unsigned b, unsigned c) {
  if (c > 0) {
    return a + x;
  } else {
    return a + b;
  }
}
EOF
```

Prerequisite: You have to install `clang` and use it to generate LLVM IR first:
```
clang -S -emit-llvm test.c
```

This will generate a `test.ll` file which should be the LLVM IR equivalent of our `test.c` file. Then we can generate EVM binary or assembly from it. In order to use the backend to generate EVM assembly, you have to specify `-mtriple=evm` when calling `llc`. An example is as follows:
```
./build/bin/llc -mtriple=evm test.ll -o test.s
```

The generated `test.s` file contains the compiled EVM assembly code. Note that the generated code is the function body itself. In order to generate a complete smart contract source code we need to use a smart contract creator function, which we will talk about it in another page.

Notice that you can also get the binary code of the function body by emitting an object file:
```
./build/bin/llc -mtriple=evm -filetype=obj test.ll -o test.o
```

#### Address layout
EVM bytecode has a flat structure. It does not have explicit function entries, nor symbol tables. All executions starts from address `0x00`. 

#### Limitations
Notice that at this moment this backend is limited to generate correct code for a single compilation unit. 

In order to link more than one compilation units, one shall inline existing compilation units in the frontend so that the frontend can generate correct `main` (the `function dispatcher` function) for the whole smart contract.

#### The function dispatcher (meta function)
The `function dispatcher` function (usually called `main` function in some contexts) is always placed at the beginning of the generated binary bytecode. The dispatcher is responsible for:
1. parse the call data and find the called function address in the jump table using the hash value provided in the call data.
2. extract the call arguments, and push them on to stack.
3. call the function address specified in the jump table.

```
 Start of address
+---------------->  +-------------------------+
                    | Function dispatcher     |
                    |   Jump Table            |
                    |    (Func1,              |
                    |     Func2,              |
                    |     Func3)              |
                    +-------------------------+
                    |                         |
                    |      Func1              |
                    |                         |
                    +-------------------------+
                    |                         |
                    |      Func2              |
                    |                         |
                    +-------------------------+
                    |                         |
                    |      Func3              |
                    |                         |
                    +-------------------------+
```

#### Moving the function dispatcher to front of the LLVM IR function list

At this moment it is up to the frontend developer to move the LLVM IR function to the beginning of the function list. You can do something like this when creating function dispatcher:
```
// Let's say you have a dispatcher function named "dispatcher"

// You should include "llvm/IR/SymbolTableListTraits.h" here
using FunctionListType = SymbolTableList<Function>;
FunctionListType &FuncList = TheModule->getFunctionList();
FuncList.remove(dispatcher);
FuncList.insert(FuncList.begin(), dispatcher);
```

# Functionalities
## Experimental support of landing pad
Landingpad is used to support exception handling.

## Experimental support of simulating heap allocations
EVM does not have a heap space, so we cannot use heap allocations. We might be able to do around it.

## Constant table support
Having a constant table in the smart contract could potentially save some code size if the elements in the table are reused.


## Metadata export
We could export more metadata for debugging, analyzing, and so on.

# Optimizations
## Support more than 16 local variables
EVM can only support retrieval of an element up to depth of 16 from the stack top using instructions `SWAP1` to `SWAP16` -- resulting a limitation in Solidity compiler that can only support 16 local variables. At this moment, EVM LLVM will also face a `stack too deep` issue if the variables in a single basic block is more than 16.

But in LLVM we can totally work around this issue, and do a much better job. With dataflow analysis and register allocation algorithm, we can have near-optimal variable assignment (on the stack or on memory stack) in linear time.

## Instruction scheduling
Arranging the order of the opcodes in EVM binary is critical to its performance. Instructions has to be arranged so that we have minimal stack manipulation over head (the opcodes that does not do actual computation, but rather, reorder stack operands' relative position to the top of stack). 

EVM LLVM backend is designed in such a way that a scheduler before register allocation can be implemented to reduce the stack operation overhead. 

## Improve EVM calling conventions
When calling a subroutine, The return address is the first argument and resides at top of stack. This is non-optimal because the return address will definitely not be used until the very end of the subroutine, and taking up a visible slot is expensive. We can re-arrange the return address to be at the end of argument so it will not have to be reached until we want to return from subroutine.

## Re-materialization of constants
usual small constants should not stay in stack --- they should be rematerialized whenever it is needed.


Ethereum Virtual Machine specific operations, such as accessing storage, retrieve block information, etc, are through EVM specific instructions. Solidity language automatically generates necessary EVM-specific instructions under the hood so as to hide the details from Solidity developers. However, as a compiler backend, the input to EVM LLVM is LLVM IR format, which is unable to hold any language specific semantics that is higher than the C language level. So it is up to compiler frontends to lower language specific semantics onto LLVM IR level. 

Intrinsic functions are used to represent EVM-specific semantics in the input LLVM IR. Intrinsic functions are usually higher level representations of architecture-specific instructions. In EVM LLVM, we allow users to leverage EVM-specific instructions that are used to interact with the chain or storage by exposing those EVM instructions in the form of intrinsic functions.

* This [page](https://github.com/etclabscore/evm_llvm/wiki/Intrinsic-Functions) lists the intrinsic functions that frontend developers can use.
* Intrinsics are defined [here](https://github.com/etclabscore/evm_llvm/blob/6271ae12899b6b9a2bfbcb3a690ec4b5e8652cfa/include/llvm/IR/IntrinsicsEVM.td#L14). 
* And here are examples on [how to leverage intrinsics](https://github.com/etclabscore/evm_llvm/blob/6271ae12899b6b9a2bfbcb3a690ec4b5e8652cfa/test/CodeGen/EVM/intrinsics.ll#L1)



![evm-llvm-green-dragon](https://user-images.githubusercontent.com/450283/63640209-85cb3c00-c66b-11e9-9610-0c339ae66ac7.png)


## EVM LLVM Wiki

Welcome to the `evm_llvm` wiki! This project aims at bringing LLVM infrastructure to the EVM world where smart contracts are widely deployed. 

EVM LLVM is an EVM architecture backend for LLVM. With EVM LLVM you can generate EVM binary code with LLVM-based compilers. The backend does not assume a language frontend, so you should be able to plug in a new smart contract language frontend to generate EVM binary.

The goal of this project is to make it able to for various of platforms, tools and smart contract programming language projects be able to quickly adapt a high-performance EVM backend. In order to produce EVM-specific instructions, a set of intrinsics must be specified for the frontend to emit so that the EVM backend understands to generate the corresponding instruction.
Many of these instructions, in Solidity, are represented by built-in functions.
**NOTE:** The argument ordering in these intrinsics may not fully match the order on which they must be pushed onto the EVM stack. Someone should double check.
**TODO:** Double-check argument and return types as well.

### SHA3 (Keccak)
-    **Name:** `llvm.evm_builtin_sha3`
-    **Arguments:**
        - `data: u256be`: byte pointer to buffer to hash
        - `len: u256be`: length of buffer
-    **Return:**
        - `hash: bytes32`: resulting hash
-    **Codegen details:**
        - SHA3 opcode: `0x20`
### Address
-    **Name:** `llvm.evm_builtin_address`
-    **Arguments:** *None*
-    **Return:**
        - `address: bytes20`: Currently executing account
-    **Codegen details:**
        - ADDRESS opcode: `0x30`
### Balance
-    **Name:** `llvm.evm_builtin_balance`
-    **Arguments:**
        - `address: bytes20`: Account to check balance of
-    **Return:**
        - `balance: u256be`: Balance of given account
-    **Codegen details:**
        - BALANCE opcode: `0x31`
### Origin
-    **Name:** `llvm.evm_builtin_origin`
-    **Arguments:** *None*
-    **Return:**
        - `address: bytes20`: Address of TX origin
-    **Codegen details**:
        - ORIGIN opcode: `0x32`
### Caller
-    **Name:** `llvm.evm_builtin_caller`
-    **Arguments:** *None*
-    **Return:**
        - `address: bytes20`: Address of caller
-    **Codegen details:**
        - CALLER opcode: `0x33`
### CallValue
-    **Name:** `llvm.evm_builtin_callvalue`
-    **Arguments:** *None*
-    **Return:**
        - `value: u256be`: TX value sent
-    **Codegen details:**
        - CALLVALUE opcode: `0x34`
### CallDataLoad
-    **Name:** `llvm.evm_builtin_calldataload`
-    **Arguments:**
        - `offset: u256be`: Offset within call data buffer to load
-    **Return:**
        - `data: bytes32`: 32 bytes of calldata beginning at `offset`
-    **Codegen details:**
        - CALLDATALOAD opcode: `0x35`
### CallDataSize
-    **Name:** `llvm.evm_builtin_calldatasize`
-    **Arguments:** *None*
-    **Return:**
        - `size: u256be`: Length of call data buffer
-    **Codegen details:**
        - CALLDATASIZE opcode: `0x36` 
### CallDataCopy
-    **Name:** `llvm.evm_builtin_calldatacopy`
-    **Arguments:**
        - `dst: u256be`: Pointer to destination buffer
        - `offset: u256be`: Offset within call data to copy
        - `len: u256be`: Number of bytes to copy beginning at `offset`
-    **Return:** *None*
-    **Codegen details:**
        - CALLDATACOPY opcode: `0x37`
### CodeSize
-    **Name:** `llvm.evm_builtin_codesize`
-    **Arguments:** *None*
-    **Return:**
        - `size: u256be`: Size of code at currently executing account
-    **Codegen details:**
        - CODESIZE opcode: `0x38`
### CodeCopy
-    **Name:** `llvm.evm_builtin_codecopy`
-    **Arguments:**
        - `dst: u256be`: Pointer to destination buffer
        - `offset: u256be`: Offset within code to copy
        - `len: u256be`: Number of bytes to copy beginning at `offset`
-    **Return:** *None*
-    **Codegen details:**
        - CODECOPY opcode: `0x39`
### GasPrice
-    **Name:** `llvm.evm_builtin_gasprice`
-    **Arguments:** *None*
-    **Return:**
        - `price: u256`: Cost of gas in current environment
-    **Codegen details:**
        - GASPRICE opcode: `0x3a`
### ExtCodeSize
-    **Name:** `llvm.evm_builtin_extcodesize`
-    **Arguments:**
        - `address: bytes20`: Address whose code size to check 
-    **Return:**
        - `size: u256be`: Size of code at given account
-    **Codegen details:**
        - EXTCODESIZE opcode: `0x3b`
 ### ExtCodeCopy
-    **Name:** `llvm.evm_builtin_extcodecopy`
-    **Arguments:**
        - `address: bytes20` Address whose code to copy
        - `dst: u256be`: Pointer to destination buffer
        - `offset: u256be`: Offset within code to copy
        - `len: u256be`: Number of bytes to copy beginning at `offset`
-    **Return:** *None*
-    **Codegen details:**
        - EXTCODECOPY opcode: `0x3C`
 ### ReturnDataSize
-    **Name:** `llvm.evm_builtin_returndatasize`
-    **Arguments:** *None*
-    **Return:**
        - `size: u256be`: Size of return data buffer from last call
-    **Codegen details:**
        - RETURNDATASIZE opcode: `0x3d`
### ReturnDataCopy
-    **Name:** `llvm.evm_builtin_returndatacopy`
-    **Arguments:**
        - `dst: u256be`: Pointer to destination buffer
        - `offset: u256be`: Offset within return buffer to copy
        - `len: u256be`: Number of bytes to copy beginning at `offset`
-    **Return:** *None*
-    **Codegen details:**
        - RETURNDATACOPY opcode: `0x3e`
### BlockHash
-    **Name:** `llvm.evm_builtin_blockhash`
-    **Arguments:**
        - `age: u256be`: The age of the block, between 0 and 256 blocks old
-    **Return:**
        - `hash: bytes32`: The hash of the requested block
-    **Codegen details:**
        -  BLOCKHASH opcode: `0x40`
### Coinbase
-    **Name:** `llvm.evm_builtin_coinbase`
-    **Arguments:** *None*
-    **Return:**
        - `beneficiary: bytes20`: Current mining beneficiary
-    **Codegen details:**
        - COINBASE opcode: `0x41`
 ### Timestamp
-    **Name:** `llvm.evm_builtin_timestamp`
-    **Arguments:** *None*
-    **Return:**
        - `stamp: u256be`: Timestamp of last block
-    **Codegen details:**
        - TIMESTAMP opcode: `0x42`
 ### Number
-    **Name:** `llvm.evm_builtin_blocknumber`
-    **Arguments:** *None*
-    **Return:**
        - `blknum: u256be`: Current block number
-    **Codegen details:**
        - NUMBER opcode: `0x43`
 ### Difficulty
-    **Name:** `llvm.evm_builtin_difficulty`
-    **Arguments:** *None*
-    **Return:**
        - `difficulty: u256be`: Current block difficulty
-    **Codegen details:**
        - DIFFICULTY opcode: `0x44`
 ### GasLimit
-    **Name:** `llvm.evm_builtin_gaslimit`
-    **Arguments:** *None*
-    **Return:**
        - `limit: u256be`: Block gas limit //NOTE: check type again
-    **Codegen details:**
        - GASLIMIT opcode: `0x45`
### StorageLoad
-    **Name:** `llvm.evm_builtin_sload`
-    **Arguments:**
        - `key: u256be`: Storage key to access
-    **Return:**
        - `value: u256be`: Storage value at key
-    **Codegen details:**
        - SLOAD opcode: `0x54`
### StorageStore
-    **Name:** `llvm.evm_builtin_sstore`
-    **Arguments:**
        - `key: u256be`: Storage key to write `value`
        - `value: u256be`: Value to write
-    **Return:** *None*
-    **Codegen details:**
        - SSTORE opcode: `0x55`
### MemSize
-    **Name:** `llvm.evm_builtin_msize`
-    **Arguments:** *None*
-    **Return:**
        - `size: u256be`: Size of active memory in bytes
-    **Codegen details:**
        - MSIZE opcode: `0x59`
### Gas
-    **Name:** `llvm.evm_builtin_gas`
-    **Arguments:** *None*
-    **Return:**
        - `gasleft: u256be`: Amount of gas left in current execution

### Log0
-    **Name:** `llvm.evm_builtin_log0`
-    **Arguments:**
        - `data: u256be`: Pointer to log data buffer
        - `len: u256be`: Size of log data buffer in bytes
-    **Return:** *None*
-    **Codegen details:**
        -  LOG0 opcode: `0xa0`
### Log1
-    **Name:** `llvm.evm_builtin_log0`
-    **Arguments:**
        - `data: u256be`: Pointer to log data buffer
        - `len: u256be`: Size of log data buffer in bytes
        - `topic1: u256be`: Log topic 1
-    **Return:** *None*
-    **Codegen details:**
        -  LOG1 opcode: `0xa1`
### Log2
-    **Name:** `llvm.evm_builtin_log2`
-    **Arguments:**
        - `data: u256be`: Pointer to log data buffer
        - `len: u256be`: Size of log data buffer in bytes
        - `topic1: u256be`: Log topic 1
        - `topic2: u256be`: Log topic 2
-    **Return:** *None*
-    **Codegen details:**
        -  LOG2 opcode: `0xa2`
### Log3
-    **Name:** `llvm.evm_builtin_log3`
-    **Arguments:**
        - `data: u256be`: Pointer to log data buffer
        - `len: u256be`: Size of log data buffer in bytes
        - `topic1: u256be`: Log topic 1
        - `topic2: u256be`: Log topic 2
        - `topic3: u256be`: Log topic 3
-    **Return:** *None*
-    **Codegen details:**
        -  LOG3 opcode: `0xa3`
### Log4
-    **Name:** `llvm.evm_builtin_log4`
-    **Arguments:**
        - `data: u256be`: Pointer to log data buffer
        - `len: u256be`: Size of log data buffer in bytes
        - `topic1: u256be`: Log topic 1
        - `topic2: u256be`: Log topic 2
        - `topic3: u256be`: Log topic 3
        - `topic4: u256be`: Log topic 4
-    **Return:** *None*
-    **Codegen details:**
        -  LOG4 opcode: `0xa4`
### Create
-    **Name:** `llvm.evm_builtin_create`
-    **Arguments:**
        - `value: u256be`: Value in wei sent to new contract
        - `code: u256be`: Pointer to code buffer for new contract
        - `len: u256be`: Code buffer length
-    **Return:**
        - `addr: bytes20`: Address of newly created contract
-    **Codegen details:**
        - CREATE opcode: `0xf0`
### Create2
-    **Name:** `llvm.evm_builtin_create2`
-    **Arguments:**
        - `value: u256be`: Value in wei sent to new contract
        - `code: u256be`: Pointer to code buffer for new contract
        - `len: u256be`: Code buffer length
        - `salt: bytes32`: Salt for address creation
-    **Return:**
        - `addr: bytes20`: Address of newly created contract
-    **Codegen details:**
        - CREATE2 opcode: `0xf5`
### Call
-    **Name:** `llvm.evm_builtin_call`
-    **Arguments:**
        - `gas: u256be`: Gas allowance for call
        - `address: bytes20`: Call destination address
        - `value: u256be`: Wei value sent with call
        - `input: u256be`: Input data pointer
        - `inputlen: u256be`: Input data buffer size
        - `output: u256be`: Output buffer pointer
        - `outputlen: u256be`: Output data buffer size
-    **Return:**
        - `return: u256be`: Exit code
-    **Codegen details:**
        - CALL opcode: `0xf1`
### CallCode
-    **Name:** `llvm.evm_builtin_callcode`
-    **Arguments:**
        - `gas: u256be`: Gas allowance for call
        - `address: bytes20`: Address of code to use
        - `value: u256be`: Wei value sent with call
        - `input: u256be`: Input data pointer
        - `inputlen: u256be`: Input data buffer size
        - `output: u256be`: Output buffer pointer
        - `outputlen: u256be`: Output data buffer size
-    **Return:**
        - `return: u256be`: Exit code
-    **Codegen details:**
        - CALL opcode: `0xf2`
### DelegateCall
-    **Name:** `llvm.evm_builtin_calldelegate`
-    **Arguments:**
        - `gas: u256be`: Gas allowance for call
        - `address: bytes20`: Address of code to use
        - `input: u256be`: Input data pointer
        - `inputlen: u256be`: Input data buffer size
        - `output: u256be`: Output buffer pointer
        - `outputlen: u256be`: Output data buffer size
-    **Return:**
        - `return: u256be`: Exit code
-    **Codegen details:**
        - CALLDELEGATE opcode: `0xf4`
### StaticCall
-    **Name:** `llvm.evm_builtin_staticcall`
-    **Arguments:**
        - `gas: u256be`: Gas allowance for call
        - `address: bytes20`: Call destination address
        - `input: u256be`: Input data pointer
        - `inputlen: u256be`: Input data buffer size
        - `output: u256be`: Output buffer pointer
        - `outputlen: u256be`: Output data buffer size
-    **Codegen details:**
        - STATICCALL opcode: `0xfa`
### Return
-    **Name:** `llvm.evm_builtin_return`
-    **Arguments:**
        - `data: u256be`: Return data pointer
        - `len: u256be`: Return buffer size
-    **Return:** *Ends execution*
-    **Codegen details:**
        - RETURN opcode: `0xf3`
### Revert
-    **Name:** `llvm.evm_builtin_revert`
-    **Arguments:**
        - `data: u256be`: Return data pointer
        - `len: u256be`: Return buffer size
-    **Return:** *Ends execution*
-    **Codegen details**
        - REVERT opcode: `0xfd`
### SelfDestruct
-    **Name:** `llvm.evm_builtin_selfdestruct`
-    **Arguments:**
        - `beneficiary: bytes20`: Address to send funds to
-    **Return:** *Ends execution*
-    **Codegen details:**
        - SELFDESTRUCT opcode: `0xff`## EVM target specific changes

### Frontend is expected to emit 256bit values LLVM IR 
The EVM architecture is the only 256-bit machine out there in the market, and so far it have not yet received support from LLVM community. We added 256-bit and 160-bit support in the LLVM IR level. 

In order to utilize 256-bit and 160-bit operands, developers are expected to emit `i256` and `i160` data types in their IR code generation. Include the `evm_llvm`'s header files in `include/llvm` folders so that these two pre-defined data types can be properly generated.

### Frontend needs to generate compatible LLVM IR
Notice that development of this backend is based on LLVM 10, which is released in March 2020. We also have a LLVM 8 branch just to support those who creates their frontends in LLVM 8.

We could do back porting to other lower versions such as LLVM 9 at the request of developers for better stability or compatibility. Please let me know if you have such needs.
EVM is different than other execution platform in that it is on blockchain. The result of the execution of a smart contract will be dependent on the state of the blockchain as well. So, we have to integrate EVM execution environment (in this early stage, `geth`) into our tests.

## Constructor
Unit tests will only focus on small test functions. But you cannot execute a function independently on blockchain, we need to have a contract constructor and dispatcher as the first function in the file. The reason is that EVM will always start its execution from address `0x00` -- where the contract header / constructor /dispatcher resides. The header then tries to set up the contract -- allocating memory/storage or parsing incoming parameters, et cetera.

Here is the commentated constructor code we use for handling unit tests:
```
define void @main() {
entry:
  %0 = call i256 @llvm.evm.calldataload(i256 0) ; extract first 32-byte argument
  %1 = call i256 @llvm.evm.calldataload(i256 32); extract second 32-byte argument
  %2 = call i256 @test(i256 %0, i256 %1)        ;  execute the unit test function
  call void @llvm.evm.mstore(i256 0, i256 %2)   ; store the returned value to memory address `0x00`
  call void @llvm.evm.return(i256 0, i256 32)   ; call "return" to return the value returned by @test
  unreachable
}
```

Notice that the `@test` function takes 2 parameters, so we will have two calls to `@llvm.evm.calldataload`.

The unit test is compiled using `llc` with options: `-mtriple=evm -filetype=obj`. Then the code is executed using `geth`'s `evm` command. 


## Testing utilities
A Python script is used to handle the testing, file `evm_llvm/tools/evm-test/evm_test.py` is the script we created  to test functionalities of the llvm backend. Here are what it does:
* call evm_llvm backend to compile an LLVM IR file (`.ll` file) into object file (`.o`) file. The file should contain the function we are going to verify along with a smart contract constructor header which is used to handle input arguments. The function should be at the beginning of the IR file (the first function).
* extract the contract opcodes from the `.o` file and prepare the input arguments (by padded each arguments to be 32 bytes long and concatenate everything into a long string).
* Run the executable binary using geth's `evm`, get the result from the print, And compare the result with expected value.

## How to run testings
1. Install Python3
2. Run `evm_llvm/tools/evm-test/evm_test.py` then you should see the results.

## How to add new tests
Please take a look at the `evm_llvm/tools/evm-test/evm_testsuit.py` file, it organizes tests by categorizing them into different `OrderedList`. Each element of the list contains the following information:
* the name of the test
* the array of input arguments
* the path of the unit test source code file (in LLVM IR form)
* the expected result value

When adding new tests, you should:
* put your test files into `evm_llvm/test/CodeGen/EVM` folder.
* add the test file path and expected results to the `evm_testsuit.py` file. (We might change it when the file gets too large).

## TODO lists
* add blockchain state related tests
* add re-entrance tests (which are also related to changes of blockchain states)

Please help improve the test utility!## Variables

In the context of stack machine, a variable refers to an operand that will be consumed by an opcode. In EVM LLVM, variables are treated as virtual registers, until they are *stackfied* (convert register-based code to stack-based code) right before lowering to machine code. 

In LLVM's internal SSA representation mode, it is fairly easy to compute a register's live range (the range from its assignment to its last use). Variables are treated differently with regard to its live range. Local variables (variables that its liveness only extends within a single basic block) will live entirely on the stack, while non-local variables (variables that live across basic blocks) will be spilled to a memory slot allocated by the compiler.

#### Frame Objects

Frame objects will be allocated either on stack or on memory space. Since each of the elements are 256bits, we have to ensure that frame objects are 256bits in length as well. Frame objects with smaller length is not supported.

It is possible for a frame object to be allocated on to memory space, if we are consuming too much of stack space. The stack allocation pass will try to find an efficient way to decide which goes to the memory and which stays in stack.


### Frame Pointer (or Free Memory Pointer)

[Stack pointers and frame pointers](https://en.wikipedia.org/wiki/Call_stack#Stack_and_frame_pointers) are essential to support subroutine calls. Frame pointer is used to record the structure of stack frames. Because we do not have registers in EVM, we will have to store stack frame pointer in memory locations. Usually, we put stack frame pointer at location `0x40`, and we follow Solidity compiler's convention to initialize it to value `128`. So the stack frame of the first function starts at that location. The value of frame pointer changes as the contract calls a subroutine or exits from a subroutine. Whenever we need to have access to frame pointer, we will retrieve its value from that specific location.

### Memory stack

Part of the memory is used as a stack for function calls and variable spills. The structure is described as follows:
* The stack goes from lower address to higher address, as different from usual hardware implementations.
* The frame is arranged into 3 parts:
    * **frame object locations**. Each frame object has its own frame slot. Frame object `x` will have a 32 byte space starting from `$fp + (x * 32)`, where `$fp` is the frame pointer, and is stored at location `0x40`.
    * **spilled variables**. Variable that are unable to be fully stackified will reside on the memory stack. In codegen, each spilled variable will have an index, and each index refers to a memory slot. A spilled variable that bears index `y`, will reside at location `$fp + (number_of_frame_objects * 32) + (y * 32)`.
    * **subroutine context**. Like a regular register machine, the memory stack is used to store subroutine context so as to support function calls. Two slots are allocated at the end of current frame for a) the existing frame pointer, and b) return `PC` address. 

 Here is an example showing a stack frame right before we jump into a subroutine:
```
  Stack top                                    Higher address
 +-----------> +----------------------------+ <--------------+
               |                            |
               |     Return Address         |
               |                            |
               +----------------------------+
               |                            |
               |     Function argument      |
   new FP      |                            |
 +-----------> +----------------------------+
               |                            |
               |    Saved frame pointer     |
               |     (Start of frame)       |
               +----------------------------+
               |                            |
               |     Stack Object 1         |
               |                            |
               +----------------------------+
               |                            |
               |     Frame Object 2         |
               |                            |
               +----------------------------+
               |                            |
               |     Frame Object 1         |
Start of frame |                            |   Lower address
+------------> +----------------------------+ <----------------+
```
The EVM architecture is a simplistic structure, but it has everything we need to do usual program computations. 

## Types of calls

There are two types of calls in an EVM smart contract:
1. **Internal calls**. Internal calls are referred to function calls within a smart contract. An example is that we have two defined function `A` and `B`, and somewhere in `A` we save our context and change our execution flow to the beginning of `B`. 
2. **External calls**. Or cross-contract calls. `A` and `B` are defined in different deployed EVM contract and `A` calls `B` in its context.

## Internal call conventions
Up to ETH 1.5, there is no link and jump EVM opcode for easy handling of subroutines(even though some [discussions](https://github.com/ethereum/EIPs/issues/2315) are on-going). So we have to manually handle subroutine calls. Here are the calling conventions for an internal calls:
* current subroutine's frame pointer is saved at stack, at memory location `$fp - 32` where `$fp` is the subroutine call's frame pointer.
* Arguments are all pushed on stack, along with the return address. Argument with smaller index number occupies a stack slot on top of another argument with a larger index number. For example, when we want to do a function call: `func abc(x, y, z)`, here is the arrangement of the arguments:

```
               +-----------+
               |Return Addr|
               +-----------+
               |     X     |
               +-----------+
               |     Y     |
               +-----------+
 Current FP    |     Z     |
+------------> +-----------+
               |  Old FP   |
               +-----------+
               |   .....   |
               +-----------+
```

*Note: Putting the return address on top of the stack is because it is easier to compute the location, but this will result in more stack manipulation overhead for the subroutine calls. We will improve this design in a later version.*
* A subroutine's return value is stored on stack top.
*Note: currently we only support one return value. In the future we will improve it by supporting multiple return values.*

## Procedure of a subroutine call
To illustrate the procedure for a subroutine call, we need to do the following to save the context of current function execution:
1. calculate the current frame size. The frame size should be the size sum of: a) slots occupied by frame objects, b) slots occupied by spilled variables, and c) one more slot for storing current frame pointer. let's assume the frame size is calculated to be `%frame_size`.
3. bump the frame pointer to: `$fp = $fp + %frame_size`. After that, we can easily restore the old frame pointer by looking at location `$fp - 32`.
4. push all subroutine arguments in order on to stack.
5. push return address onto stack. (At this moment, the return address is `PC + 6`). 
6. push the beginning address of subroutine and jump.

Right before we return from a subroutine, the stack should be empty and the return address should be at the top of the stack. When returning from a subroutine call, we should do the following:
1. push return value on to top of stack.
2. Do a `swap1` to move the return address to top of stack
3. jump to return address and resume the execution in caller function.
If the function returns nothing, simply jump to return address.

After jumping back to caller, we have to resume the execution:
1. restore caller's frame pointer by storing the value at location `$fp - 32` to `0x40`.

## [EIP2315](https://eips.ethereum.org/EIPS/eip-2315) Support: Subroutine calls

The support of subroutines inside EVM enables compiler to generate better performance code. To be more specific: With EIP235, it is up to EVM to maintain the stack:
1. the return address stack is only accessible to VM
2. the stack is invisible to users and compilers

A better calling convention is made with the support of EIP2315:

### To generate a call procedure
1. calculate the current frame size. The frame size should be the size sum of: a) slots occupied by frame objects, b) slots occupied by spilled variables, and c) one more slot for storing current frame pointer. let's assume the frame size is calculated to be `%frame_size`.
2. save existing frame pointer at memory location `$fp + %frame_size - 32`. The frame pointer is maintained at `0x40`.
3. bump the frame pointer to: `$fp = $fp + %frame_size`. After that, we can easily restore the old frame pointer by looking at location `$fp - 32`.
4. push all subroutine arguments in order on to stack.
5. push the beginning address of subroutine and call `JUMPSUB`

### To generate the return 
1. push return value on to top of stack.
3. call `RETURNSUB` to resume execution of caller function.

## External calls
External calls are implemented using intrinsic calls.## Newly supported Types
So far the open-source LLVM trunk has not yet implemented bit size support larger than 128bits. We have implemented 256bit supports in our own backend, and is considering contributing them back to main trunk.

Users are allowed to use `i256` and `i160` data types in their generated LLVM IR, which represent 256bit integer types and 160bit integer types respectively.

Even though all EVM data types are 256bit in length internally. We are still able to offer support to smaller data types. However, users are encouraged to use 256bit data types internally because it is free.

## Contract Input Argument Types -- The Solidity convention 

Contract arguments are passed to EVM via the call data field. The function dispatcher is responsible to extract input arguments from call data. 

In Solidity's convention, the arguments in call data are padded to 32 bytes long if its data type's length is shorter. So, in order to maintain the convention, the function dispatcher needs to truncate the input arguments to the defined size in the function that is going to be called.

This is undoubtedly inefficient, so users are discouraged to use smaller data types. 
