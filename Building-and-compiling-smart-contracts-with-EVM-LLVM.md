It is the frontend's responsibility to do the smart contract's plumbing, including the contract's constructor function. 

### The Contract constructor function
Because EVM's execution always start from the beginning of the code (`pc = 0`), there must be a way to handle more complicated contract behaviors. In EVM LLVM, we use a function to describe the function handling. It is called contract constructor function. To implement the function, developers are expected to respect the following contract constructor properties:
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
  %0 = call i256 @llvm.evm.calldataload(i256 0)
  %1 = call i256 @llvm.evm.calldataload(i256 1)
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

