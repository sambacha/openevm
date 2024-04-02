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

A Python script is used to handle the testing, file `evm_llvm/tools/evm-test/evm_test.py` is the script we created to test functionalities of the llvm backend. Here are what it does:

-   call evm_llvm backend to compile an LLVM IR file (`.ll` file) into object file (`.o`) file. The file should contain the function we are going to verify along with a smart contract constructor header which is used to handle input arguments. The function should be at the beginning of the IR file (the first function).
-   extract the contract opcodes from the `.o` file and prepare the input arguments (by padded each arguments to be 32 bytes long and concatenate everything into a long string).
-   Run the executable binary using geth's `evm`, get the result from the print, And compare the result with expected value.

## How to run testings

1. Install Python3
2. Run `evm_llvm/tools/evm-test/evm_test.py` then you should see the results.

## How to add new tests

Please take a look at the `evm_llvm/tools/evm-test/evm_testsuit.py` file, it organizes tests by categorizing them into different `OrderedList`. Each element of the list contains the following information:

-   the name of the test
-   the array of input arguments
-   the path of the unit test source code file (in LLVM IR form)
-   the expected result value

When adding new tests, you should:

-   put your test files into `evm_llvm/test/CodeGen/EVM` folder.
-   add the test file path and expected results to the `evm_testsuit.py` file. (We might change it when the file gets too large).

## TODO lists

-   add blockchain state related tests
-   add re-entrance tests (which are also related to changes of blockchain states)

Please help improve the test utility!
