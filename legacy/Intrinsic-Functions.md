In order to produce EVM-specific instructions, a set of intrinsics must be specified for the frontend to emit so that the EVM backend understands to generate the corresponding instruction. Many of these instructions, in Solidity, are represented by built-in functions. **NOTE:** The argument ordering in these intrinsics may not fully match the order on which they must be pushed onto the EVM stack. Someone should double check. **TODO:** Double-check argument and return types as well.

### SHA3 (Keccak)

-   **Name:** `llvm.evm_builtin_sha3`
-   **Arguments:**
    -   `data: u256be`: byte pointer to buffer to hash
    -   `len: u256be`: length of buffer
-   **Return:**
    -   `hash: bytes32`: resulting hash
-   **Codegen details:**
    -   SHA3 opcode: `0x20`

### Address

-   **Name:** `llvm.evm_builtin_address`
-   **Arguments:** _None_
-   **Return:**
    -   `address: bytes20`: Currently executing account
-   **Codegen details:**
    -   ADDRESS opcode: `0x30`

### Balance

-   **Name:** `llvm.evm_builtin_balance`
-   **Arguments:**
    -   `address: bytes20`: Account to check balance of
-   **Return:**
    -   `balance: u256be`: Balance of given account
-   **Codegen details:**
    -   BALANCE opcode: `0x31`

### Origin

-   **Name:** `llvm.evm_builtin_origin`
-   **Arguments:** _None_
-   **Return:**
    -   `address: bytes20`: Address of TX origin
-   **Codegen details**:
    -   ORIGIN opcode: `0x32`

### Caller

-   **Name:** `llvm.evm_builtin_caller`
-   **Arguments:** _None_
-   **Return:**
    -   `address: bytes20`: Address of caller
-   **Codegen details:**
    -   CALLER opcode: `0x33`

### CallValue

-   **Name:** `llvm.evm_builtin_callvalue`
-   **Arguments:** _None_
-   **Return:**
    -   `value: u256be`: TX value sent
-   **Codegen details:**
    -   CALLVALUE opcode: `0x34`

### CallDataLoad

-   **Name:** `llvm.evm_builtin_calldataload`
-   **Arguments:**
    -   `offset: u256be`: Offset within call data buffer to load
-   **Return:**
    -   `data: bytes32`: 32 bytes of calldata beginning at `offset`
-   **Codegen details:**
    -   CALLDATALOAD opcode: `0x35`

### CallDataSize

-   **Name:** `llvm.evm_builtin_calldatasize`
-   **Arguments:** _None_
-   **Return:**
    -   `size: u256be`: Length of call data buffer
-   **Codegen details:**
    -   CALLDATASIZE opcode: `0x36`

### CallDataCopy

-   **Name:** `llvm.evm_builtin_calldatacopy`
-   **Arguments:**
    -   `dst: u256be`: Pointer to destination buffer
    -   `offset: u256be`: Offset within call data to copy
    -   `len: u256be`: Number of bytes to copy beginning at `offset`
-   **Return:** _None_
-   **Codegen details:**
    -   CALLDATACOPY opcode: `0x37`

### CodeSize

-   **Name:** `llvm.evm_builtin_codesize`
-   **Arguments:** _None_
-   **Return:**
    -   `size: u256be`: Size of code at currently executing account
-   **Codegen details:**
    -   CODESIZE opcode: `0x38`

### CodeCopy

-   **Name:** `llvm.evm_builtin_codecopy`
-   **Arguments:**
    -   `dst: u256be`: Pointer to destination buffer
    -   `offset: u256be`: Offset within code to copy
    -   `len: u256be`: Number of bytes to copy beginning at `offset`
-   **Return:** _None_
-   **Codegen details:**
    -   CODECOPY opcode: `0x39`

### GasPrice

-   **Name:** `llvm.evm_builtin_gasprice`
-   **Arguments:** _None_
-   **Return:**
    -   `price: u256`: Cost of gas in current environment
-   **Codegen details:**
    -   GASPRICE opcode: `0x3a`

### ExtCodeSize

-   **Name:** `llvm.evm_builtin_extcodesize`
-   **Arguments:**
    -   `address: bytes20`: Address whose code size to check
-   **Return:**
    -   `size: u256be`: Size of code at given account
-   **Codegen details:**
    -   EXTCODESIZE opcode: `0x3b`

### ExtCodeCopy

-   **Name:** `llvm.evm_builtin_extcodecopy`
-   **Arguments:**
    -   `address: bytes20` Address whose code to copy
    -   `dst: u256be`: Pointer to destination buffer
    -   `offset: u256be`: Offset within code to copy
    -   `len: u256be`: Number of bytes to copy beginning at `offset`
-   **Return:** _None_
-   **Codegen details:**
    -   EXTCODECOPY opcode: `0x3C`

### ReturnDataSize

-   **Name:** `llvm.evm_builtin_returndatasize`
-   **Arguments:** _None_
-   **Return:**
    -   `size: u256be`: Size of return data buffer from last call
-   **Codegen details:**
    -   RETURNDATASIZE opcode: `0x3d`

### ReturnDataCopy

-   **Name:** `llvm.evm_builtin_returndatacopy`
-   **Arguments:**
    -   `dst: u256be`: Pointer to destination buffer
    -   `offset: u256be`: Offset within return buffer to copy
    -   `len: u256be`: Number of bytes to copy beginning at `offset`
-   **Return:** _None_
-   **Codegen details:**
    -   RETURNDATACOPY opcode: `0x3e`

### BlockHash

-   **Name:** `llvm.evm_builtin_blockhash`
-   **Arguments:**
    -   `age: u256be`: The age of the block, between 0 and 256 blocks old
-   **Return:**
    -   `hash: bytes32`: The hash of the requested block
-   **Codegen details:**
    -   BLOCKHASH opcode: `0x40`

### Coinbase

-   **Name:** `llvm.evm_builtin_coinbase`
-   **Arguments:** _None_
-   **Return:**
    -   `beneficiary: bytes20`: Current mining beneficiary
-   **Codegen details:**
    -   COINBASE opcode: `0x41`

### Timestamp

-   **Name:** `llvm.evm_builtin_timestamp`
-   **Arguments:** _None_
-   **Return:**
    -   `stamp: u256be`: Timestamp of last block
-   **Codegen details:**
    -   TIMESTAMP opcode: `0x42`

### Number

-   **Name:** `llvm.evm_builtin_blocknumber`
-   **Arguments:** _None_
-   **Return:**
    -   `blknum: u256be`: Current block number
-   **Codegen details:**
    -   NUMBER opcode: `0x43`

### Difficulty

-   **Name:** `llvm.evm_builtin_difficulty`
-   **Arguments:** _None_
-   **Return:**
    -   `difficulty: u256be`: Current block difficulty
-   **Codegen details:**
    -   DIFFICULTY opcode: `0x44`

### GasLimit

-   **Name:** `llvm.evm_builtin_gaslimit`
-   **Arguments:** _None_
-   **Return:**
    -   `limit: u256be`: Block gas limit //NOTE: check type again
-   **Codegen details:**
    -   GASLIMIT opcode: `0x45`

### StorageLoad

-   **Name:** `llvm.evm_builtin_sload`
-   **Arguments:**
    -   `key: u256be`: Storage key to access
-   **Return:**
    -   `value: u256be`: Storage value at key
-   **Codegen details:**
    -   SLOAD opcode: `0x54`

### StorageStore

-   **Name:** `llvm.evm_builtin_sstore`
-   **Arguments:**
    -   `key: u256be`: Storage key to write `value`
    -   `value: u256be`: Value to write
-   **Return:** _None_
-   **Codegen details:**
    -   SSTORE opcode: `0x55`

### MemSize

-   **Name:** `llvm.evm_builtin_msize`
-   **Arguments:** _None_
-   **Return:**
    -   `size: u256be`: Size of active memory in bytes
-   **Codegen details:**
    -   MSIZE opcode: `0x59`

### Gas

-   **Name:** `llvm.evm_builtin_gas`
-   **Arguments:** _None_
-   **Return:**
    -   `gasleft: u256be`: Amount of gas left in current execution

### Log0

-   **Name:** `llvm.evm_builtin_log0`
-   **Arguments:**
    -   `data: u256be`: Pointer to log data buffer
    -   `len: u256be`: Size of log data buffer in bytes
-   **Return:** _None_
-   **Codegen details:**
    -   LOG0 opcode: `0xa0`

### Log1

-   **Name:** `llvm.evm_builtin_log0`
-   **Arguments:**
    -   `data: u256be`: Pointer to log data buffer
    -   `len: u256be`: Size of log data buffer in bytes
    -   `topic1: u256be`: Log topic 1
-   **Return:** _None_
-   **Codegen details:**
    -   LOG1 opcode: `0xa1`

### Log2

-   **Name:** `llvm.evm_builtin_log2`
-   **Arguments:**
    -   `data: u256be`: Pointer to log data buffer
    -   `len: u256be`: Size of log data buffer in bytes
    -   `topic1: u256be`: Log topic 1
    -   `topic2: u256be`: Log topic 2
-   **Return:** _None_
-   **Codegen details:**
    -   LOG2 opcode: `0xa2`

### Log3

-   **Name:** `llvm.evm_builtin_log3`
-   **Arguments:**
    -   `data: u256be`: Pointer to log data buffer
    -   `len: u256be`: Size of log data buffer in bytes
    -   `topic1: u256be`: Log topic 1
    -   `topic2: u256be`: Log topic 2
    -   `topic3: u256be`: Log topic 3
-   **Return:** _None_
-   **Codegen details:**
    -   LOG3 opcode: `0xa3`

### Log4

-   **Name:** `llvm.evm_builtin_log4`
-   **Arguments:**
    -   `data: u256be`: Pointer to log data buffer
    -   `len: u256be`: Size of log data buffer in bytes
    -   `topic1: u256be`: Log topic 1
    -   `topic2: u256be`: Log topic 2
    -   `topic3: u256be`: Log topic 3
    -   `topic4: u256be`: Log topic 4
-   **Return:** _None_
-   **Codegen details:**
    -   LOG4 opcode: `0xa4`

### Create

-   **Name:** `llvm.evm_builtin_create`
-   **Arguments:**
    -   `value: u256be`: Value in wei sent to new contract
    -   `code: u256be`: Pointer to code buffer for new contract
    -   `len: u256be`: Code buffer length
-   **Return:**
    -   `addr: bytes20`: Address of newly created contract
-   **Codegen details:**
    -   CREATE opcode: `0xf0`

### Create2

-   **Name:** `llvm.evm_builtin_create2`
-   **Arguments:**
    -   `value: u256be`: Value in wei sent to new contract
    -   `code: u256be`: Pointer to code buffer for new contract
    -   `len: u256be`: Code buffer length
    -   `salt: bytes32`: Salt for address creation
-   **Return:**
    -   `addr: bytes20`: Address of newly created contract
-   **Codegen details:**
    -   CREATE2 opcode: `0xf5`

### Call

-   **Name:** `llvm.evm_builtin_call`
-   **Arguments:**
    -   `gas: u256be`: Gas allowance for call
    -   `address: bytes20`: Call destination address
    -   `value: u256be`: Wei value sent with call
    -   `input: u256be`: Input data pointer
    -   `inputlen: u256be`: Input data buffer size
    -   `output: u256be`: Output buffer pointer
    -   `outputlen: u256be`: Output data buffer size
-   **Return:**
    -   `return: u256be`: Exit code
-   **Codegen details:**
    -   CALL opcode: `0xf1`

### CallCode

-   **Name:** `llvm.evm_builtin_callcode`
-   **Arguments:**
    -   `gas: u256be`: Gas allowance for call
    -   `address: bytes20`: Address of code to use
    -   `value: u256be`: Wei value sent with call
    -   `input: u256be`: Input data pointer
    -   `inputlen: u256be`: Input data buffer size
    -   `output: u256be`: Output buffer pointer
    -   `outputlen: u256be`: Output data buffer size
-   **Return:**
    -   `return: u256be`: Exit code
-   **Codegen details:**
    -   CALL opcode: `0xf2`

### DelegateCall

-   **Name:** `llvm.evm_builtin_calldelegate`
-   **Arguments:**
    -   `gas: u256be`: Gas allowance for call
    -   `address: bytes20`: Address of code to use
    -   `input: u256be`: Input data pointer
    -   `inputlen: u256be`: Input data buffer size
    -   `output: u256be`: Output buffer pointer
    -   `outputlen: u256be`: Output data buffer size
-   **Return:**
    -   `return: u256be`: Exit code
-   **Codegen details:**
    -   CALLDELEGATE opcode: `0xf4`

### StaticCall

-   **Name:** `llvm.evm_builtin_staticcall`
-   **Arguments:**
    -   `gas: u256be`: Gas allowance for call
    -   `address: bytes20`: Call destination address
    -   `input: u256be`: Input data pointer
    -   `inputlen: u256be`: Input data buffer size
    -   `output: u256be`: Output buffer pointer
    -   `outputlen: u256be`: Output data buffer size
-   **Codegen details:**
    -   STATICCALL opcode: `0xfa`

### Return

-   **Name:** `llvm.evm_builtin_return`
-   **Arguments:**
    -   `data: u256be`: Return data pointer
    -   `len: u256be`: Return buffer size
-   **Return:** _Ends execution_
-   **Codegen details:**
    -   RETURN opcode: `0xf3`

### Revert

-   **Name:** `llvm.evm_builtin_revert`
-   **Arguments:**
    -   `data: u256be`: Return data pointer
    -   `len: u256be`: Return buffer size
-   **Return:** _Ends execution_
-   **Codegen details**
    -   REVERT opcode: `0xfd`

### SelfDestruct

-   **Name:** `llvm.evm_builtin_selfdestruct`
-   **Arguments:**
    -   `beneficiary: bytes20`: Address to send funds to
-   **Return:** _Ends execution_
-   **Codegen details:**
    -   SELFDESTRUCT opcode: `0xff`
