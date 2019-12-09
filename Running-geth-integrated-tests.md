EVM is different than other execution platform in that it is on blockchain. The result of the execution of a smart contract will be dependent on the state of the blockchain as well. So, we have to integrate EVM execution environment into our tests.

## Scripting the constructor
Unit tests will only focus on small test functions. But you cannot execute a function independently on blockchain, we need to have a contract constructor and dispatcher.

The EVM will always start its execution from address `0x00` -- where the contract header / constructor /dispatcher resides. The header then tries to set up the contract -- allocating memory/storage or parsing incoming parameters, et cetera.

Changes in the header will likely change the predefined locations and behaviours in the contract. Before we are capable of handling embedded assembly, we will use a pre-defined header, and and external assembler to do the contract creation and verification.

## Testing utilities
A Python script is used to handle the testing, file `evm_llvm/tools/evm-test/evm_test.py` is the script we created  to test functionalities of the llvm backend. Here are what it does:
* call evm_llvm backend to compile an LLVM IR file (`.ll` file) into assembly file (`.s`) file. The file should contain the function we are going to verify. The function should be at the beginning of the IR file (the first function).
* Append the predefined contract header to the generated `.s` file. The header handles pushing operands onto the execution stack, or parse input data from the chain, etc.
* Call a third party assembler `pyevmasm` (I had to modify the original code to make it able to do realistic functions such as supporting labels, etc, my branch is at `https://github.com/lialan/pyevmasm`) to generate the executable binary. 
* Run the executable binary using geth's `evm`. And compare the results.

## How to run testings
1. At this moment, it is dependent on `https://github.com/lialan/pyevmasm` so you need to have it on your Python path (or install it). 
2. Run `evm_llvm/tools/evm-test/evm_test.py` then you should see the results.

## How to add new tests
Please take a look at the `evm_llvm/tools/evm-test/evm_test.py` file. You should:
* put your test files into `evm_llvm/test/CodeGen/EVM` folder.
* add the test file path and expected results to the `evm_test.py` file. (We might change it when the file gets too large).

## TODO lists
* add blockchain state related tests
* add re-entrance tests