# Core Paper Project of EVM

The Core Paper Project of EVM aims at providing a modular and general
purpose specification for Ethereum Virtual Machine. Ethereum Virtual
Machine, or EVM, is a widely used stack-based virtual machine and
binary instruction format.

EVM is initially designed for Ethereum and Ethereum Classic, with VM
structures specific to those blockchains. However, it's being adopted
in a wide range of other projects, such as [Parity
Substrate](https://github.com/paritytech/substrate/pull/3927). Those
projects have vastly different requirements compared with Ethereum and
Ethereum Classic, and as a result, they would benefit from a
standalone specification process.

We design this specification to be modular, from a basic layer caller
EVM Core, which has minimal assumptions about the environment. Modules
are then provided on top of EVM Core, which makes additional
assumptions about the environment. Many layers, including EVM Core,
does not contain the gasometer, which means it's suitable to be used
in general-purpose environments and is much easier to be implemented.

## EVM Core

EVM Core defines the base layer of execution. The VM has access to the
following information:

- **Data**: a bytearray defining the input of the VM.
- **Code**: a bytearray defining the code being executed.
- **Program Counter**: an integer, pointing to the position of the
  next instruction being executed.
- **Jump Validity Map**: a boolean list the same size as the code
  bytearray. It is generated in the beginning of the program
  execution, and sets all valid `JUMPDEST` position to true.
- **Memory**: A linear memory of bytes, of given limit.
- **Stack**: A stack, containing values of 256-bit.

Valid instructions of EVM Core are:

- **Stop and Arithmetic**: `STOP`, `ADD`, `MUL`, `SUB`, `DIV`, `SDIV`,
  `MOD`, `SMOD`, `ADDMOD`, `MULMOD`, `EXP`, `SIGNEXTEND`.
- **Comparison and Bitwise Logic**: `LT`, `GT`, `SLT`, `SGT`, `EQ`,
  `ISZERO`, `AND`, `OR`, `XOR`, `NOT`, `BYTE`, `SHL`, `SHR`, `SAR`.
- **Code and Data Access**: `CALLDATALOAD`, `CALLDATASIZE`,
  `CALLDATACOPY`, `CODESIZE`, `CODECOPY`.
- **Stack, Memory and Flow Control**: `POP`, `PUSHn`, `DUPn`, `SWAPn`,
  `MLOAD`, `MSTORE`, `MSTORE8`, `JUMP`, `JUMPI`, `PC`, `MSIZE`,
  `JUMPDEST`, `RETURN`, `REVERT`, `INVALID`.
  
## EVM ROM

The EVM ROM layer can be built on top of EVM Core to provide access to
a range of read-only memory. We define the following structure:

- **Read-only Memory**: A range of read-only memory that can be
  accessed by specific opcodes.
  
We redefine the following opcodes to be access of read-only
memory. Here we define read-only memory to have index every 32 bytes.

- `ADDRESS` (`0x30`): `READROM 0x0` Push index `0` of read-only memory onto stack.
- `ORIGIN` (`0x32`): `READROM 0x1` Push index `1` of read-only memory onto stack.
- `CALLER` (`0x33`): `READROM 0x3` Push index `2` of read-only memory onto stack.
- `CALLVALUE` (`0x34`): `READROM 0x4` Push index `3` of read-only memory onto stack.
- `GASPRICE` (`0x3a`): `READROM 0x5` Push index `4` of read-only memory onto stack.
- `COINBASE` (`0x41`): `READROM 0x6` Push index `5` of read-only memory onto stack.
- `TIMESTAMP` (`0x42`): `READROM 0x7` Push index `6` of read-only memory onto stack.
- `NUMBER` (`0x43`): `READROM 0x8` Push index `7` of read-only memory onto stack.
- `DIFFICULTY` (`0x44`): `READROM 0x9` Push index `8` of read-only memory onto stack.
- `GASLIMIT` (`0x45`): `READROM 0xa` Push index `9` of read-only memory onto stack.
- `CHAINID` (`0x46`): `READROM 0xb` Push index `10` of read-only memory onto stack.
- `SELFBALANCE` (`0x47`): `READROM 0xc` Push index `11` of read-only memory onto stack.

## EVM Storage

The EVM Storage layer provides opcodes for access of a persistent
storage:

- **Storage**: External storage that can be read or write by the
  contract.
  
Opcodes `SLOAD` and `SSTORE` are defined in this layer.

## EVM Log

The EVM Log layer provides opcodes for logging:

- **Log**: Append-only data structure with structure `{ topics:
  Vec<H256>, data: Vec<u8> }`, where `topics` can at most be length 4.
  
Opcodes `LOGn` are defined in this layer.

## EVM Ethereum

We define all Ethereum specific opcodes in this layer. This includes:

- **Sha3**: `SHA3`
- **Environmental Information**: `BALANCE`, `EXTCODESIZE`,
  `EXTCODECOPY`
- **Block Information**: `BLOCKHASH`
- **Gasometer**: `GAS`
- **System Operations**: `CREATE`, `CREATE2`, `CALL`, `CALLCODE`,
  `DELEGATECALL`, `STATICCALL`
